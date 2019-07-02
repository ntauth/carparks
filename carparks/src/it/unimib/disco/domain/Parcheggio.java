package it.unimib.disco.domain;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import it.unimib.disco.net.ParcheggioSocketClient;
import it.unimib.disco.net.message.ParcheggioNetMessage;
import it.unimib.disco.net.serialization.JsonSerializationPolicy;
import it.unimib.disco.utils.ListUtils;

public class Parcheggio extends Observable implements Callable<Void> {
	
	public static final int RESERVATION_TIME_SLOT_COUNT = 48;
	
	protected static final long PARKSLOT_SEMAPHORE_ACQUIRE_TIMEOUT = 500L;
	protected static final long VALETS_SEMAPHORE_ACQUIRE_TIMEOUT = 500L;
	
	protected static final int PARKING_SLOT_OCCUPIED_INDEFINITELY = Integer.MAX_VALUE;
	
	protected int id;
	protected String name;

	protected int reservationTimeSlotCount;
	protected Map<Integer, List<Boolean>> parkingSlotTimeSlotsMap;
	protected Semaphore freeParkingSlotsSemaphore;
	protected long freeParkingSlotsSemaphoreAcquireTimeout;
	
	protected Queue<Parcheggiatore> valets;
	protected Semaphore valetsSemaphore;
	protected long valetsSemaphoreAcquireTimeout;
	
	protected Map<Ticket, Automobile> ticketAutomobileMap;
	protected Map<Ticket, Integer> ticketParkingSlotMap;
	
	protected Object requestsMonitor;
	protected ConcurrentLinkedQueue<RitiroRequest> ritiroRequests;
	protected ConcurrentLinkedQueue<RitiroWithTicketRequest> ritiroWithTicketRequests;
	protected ConcurrentLinkedQueue<RestituzioneRequest> restituzioneRequests;
	
	protected ExecutorService threadPoolService;
	protected ParcheggioSocketClient platformSocket;

	public Parcheggio(int freeParkingSlots, List<Parcheggiatore> valets) {
		this(UUID.randomUUID().hashCode(), "", freeParkingSlots, valets);
	}
	
	public Parcheggio(int id, String name, int freeParkingSlots, List<Parcheggiatore> valets) {
		
		// Info
		this.id = id;
		this.name = name;
		
		// Parking slots
		this.parkingSlotTimeSlotsMap = new ConcurrentHashMap<>();
		this.reservationTimeSlotCount = RESERVATION_TIME_SLOT_COUNT;
			
		for (int i = 1; i <= freeParkingSlots; i++) {
			
			List<Boolean> freeTimeSlots = new ArrayList<>();
			
			for (int j = 1; j <= this.reservationTimeSlotCount; j++)
				freeTimeSlots.add(true);
			
			this.parkingSlotTimeSlotsMap.put(i, freeTimeSlots);
		}
		
		this.freeParkingSlotsSemaphore = new Semaphore(freeParkingSlots, true);
		this.freeParkingSlotsSemaphoreAcquireTimeout = PARKSLOT_SEMAPHORE_ACQUIRE_TIMEOUT;
		
		// Valets
		this.valetsSemaphore = new Semaphore(valets.size(), true);
		this.valets = new LinkedList<>();
		this.valetsSemaphoreAcquireTimeout = VALETS_SEMAPHORE_ACQUIRE_TIMEOUT;
		
		// Inject the valets dependency
		for (Parcheggiatore v : valets)
			this.valets.add(v);
		
		// Tickets
		this.ticketAutomobileMap = new ConcurrentHashMap<>();
		this.ticketParkingSlotMap = new ConcurrentHashMap<>();
		
		// Requests
		this.requestsMonitor = new Object();
		this.ritiroRequests = new ConcurrentLinkedQueue<>();
		this.ritiroWithTicketRequests = new ConcurrentLinkedQueue<>();
		this.restituzioneRequests = new ConcurrentLinkedQueue<>();
		
		// Thread pool for valets and net watchdog
		this.threadPoolService = Executors.newFixedThreadPool(valets.size() + 1);
		this.platformSocket = new ParcheggioSocketClient(new JsonSerializationPolicy());
	}
	
	/**
	 * @brief Processes a @see NetMessage
	 * 
	 * @throws IOException
	 */
	protected void processNetMessage(ParcheggioNetMessage msg) throws IOException {
		
		switch (msg.getType()) {
				
			case RESERVE_TIME_SLOT:		
			
				msg.setTicket(onReserve(msg.getTimeSlotStart(), msg.getTimeSlotEnd()));
				platformSocket.writeObject(msg);
				
				break;
				
			default:
				break;
		}
	}
	
	/**
	 * @brief Connects the @see Parcheggio instance to the platform server
	 * 
	 */
	public void connectToPlatform(String ip, int port) {
	
		platformSocket.connect(ip, port);
		
		// Add the platform as a remote observer
		addObserver((o, s) -> {
			
			try {
				platformSocket.sendSnapshot((Parcheggio.Snapshot) s);
			}
			catch (Exception e) {

			}
		});
		
		// Update the observers, if any
		onParcheggioUpdate();
	}
	
	@Override
	public Void call() throws Exception {
		
		// Launch valets
		for (Parcheggiatore v : valets)
			threadPoolService.submit(v);
		
		// Launch the net request watchdog
		threadPoolService.submit(() -> netRequestWatchdog());
		
		// Launch the local request watchdog
		requestWatchdog();
		
		return null;
	}
	
	/**
	 * @brief Watchdog for net requests (requests coming from the platform server)
	 * 
	 */
	protected void netRequestWatchdog() {
	
		while (true) {
			
			try {
				
				Object message_ = platformSocket.readObject(ParcheggioNetMessage.class);
				ParcheggioNetMessage message = (ParcheggioNetMessage) message_;
				
				if (message != null)
					processNetMessage(message);
			}
			catch (Exception e) {

			}
		}
	}
	
	/**
	 * @brief Watchdog for local requests
	 * 
	 */
	protected void requestWatchdog() throws InterruptedException {
		
		while (true) {		
				
			//region Interlocked
			synchronized (requestsMonitor) {
				
				int cumulativeSize = ritiroRequests.size() + restituzioneRequests.size();
				
				while (cumulativeSize == 0) {
					
					requestsMonitor.wait();
					cumulativeSize = ritiroRequests.size() + restituzioneRequests.size();
				}
			}
			//endregion
			
			RitiroRequest rireq = ritiroRequests.poll();
			
			if (rireq != null) {
				
				if (!rireq.isBeingHandled())		
					onRitira(rireq);
				else if (!rireq.isFulfilled())
					ritiroRequests.add(rireq);
			}
			
			RestituzioneRequest rereq = restituzioneRequests.poll();
			
			if (rereq != null) {
				
				if (!rereq.isBeingHandled())
					onRestituisci(rereq);
				else if (!rereq.isFulfilled())
					restituzioneRequests.add(rereq);
			}
		}
	}
	
	/**
	 * @brief Handles a @see RitiroRequest pulled from the originating message mailbox
	 * 
	 * @throws InterruptedException
	 */
	protected void onRitira(RitiroRequest request) throws InterruptedException {
		
		boolean slotAcquired = freeParkingSlotsSemaphore.tryAcquire(freeParkingSlotsSemaphoreAcquireTimeout, TimeUnit.MICROSECONDS);
		
		if (slotAcquired) {
			
			// Find the first free parking slot
			int freeParkingSlot = -1;
			
			//region Interlocked
			synchronized (parkingSlotTimeSlotsMap) {
				
				Optional<Integer> firstFreeParkingSlot =
						parkingSlotTimeSlotsMap.entrySet().stream()
												.filter(x -> ListUtils.getTrueBitsCount(x.getValue())
																== this.reservationTimeSlotCount)
												.map(x -> x.getKey())
												.findFirst();
				
				if (firstFreeParkingSlot.isPresent()) {
	
					freeParkingSlot = firstFreeParkingSlot.get();
					parkingSlotTimeSlotsMap.put(freeParkingSlot, ListUtils.bitwiseNot(
								ListUtils.makeAdjacencyBitmask(this.reservationTimeSlotCount,
																0,
																this.reservationTimeSlotCount - 1)));
				}
			}
			//endregion
			
			if (freeParkingSlot != -1)
				onRitiraAcquireValetAndFulfill(request, freeParkingSlot);
			else
				request.fulfill(null);
		}
	}
	
	/**
	 * @brief Handles a @see RitiroWithTicketRequest pulled from the originating message mailbox
	 * 
	 * @throws InterruptedException
	 */
	protected void onRitiraWithTicket(RitiroWithTicketRequest request) throws InterruptedException {
		
		boolean slotAcquired = freeParkingSlotsSemaphore.tryAcquire(freeParkingSlotsSemaphoreAcquireTimeout, TimeUnit.MICROSECONDS);

		if (slotAcquired) {
			
			assert ticketParkingSlotMap.containsKey(request.getPayloadTicket());
			onRitiraAcquireValetAndFulfill(request, ticketParkingSlotMap.get(request.getPayloadTicket()));
		}
	}

	/**
	 * @brief Attempts to acquire a valet and fulfill a @see RitiroRequest
	 * 
	 * @throws InterruptedException
	 */
	protected void onRitiraAcquireValetAndFulfill(RitiroRequest request, int parkingSlot) throws InterruptedException {

		boolean valetAcquired = valetsSemaphore.tryAcquire(valetsSemaphoreAcquireTimeout, TimeUnit.MICROSECONDS);
		
		//region Interlocked
		if (valetAcquired) {
			
			boolean handleAcquired = request.handle();
			
			if (handleAcquired) {
				
				Parcheggiatore valet = valets.poll();
				RitiroRequest requestProxy = new RitiroRequest(request.getPayload());
				Ticket ticket;
				
				// If RitiroWithTicket, don't create a new Ticket
				if (request instanceof RitiroWithTicketRequest)
					ticket = ((RitiroWithTicketRequest) request).getPayloadTicket();
				else
					ticket = new Ticket();
				
				requestProxy.addObserver((o, none) -> {
					
					ticketAutomobileMap.put((Ticket) ticket, request.getPayload());
					ticketParkingSlotMap.put((Ticket) ticket, parkingSlot);
					
					request.fulfill(ticket);
					
					valetsSemaphore.release();
					
					onParcheggioUpdate();
				});
				
				valet.ritira(requestProxy);
				valets.add(valet);
			}
			else {
				
				valetsSemaphore.release();
				freeParkingSlotsSemaphore.release();
			}
		}
		//endregion
	}
	
	/**
	 * @brief Handles a @see RestituzioneRequest pulled from the originating message mailbox
	 * 
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 */
	protected void onRestituisci(RestituzioneRequest request) throws InterruptedException, IllegalArgumentException {
		
		Ticket ticket = request.getPayload();
		
		if (!ticketParkingSlotMap.containsKey(ticket)) {
			
			request.fulfill(null);
			throw new IllegalArgumentException("Ticket isn't present");
		}
		
		boolean valetAcquired = valetsSemaphore.tryAcquire(valetsSemaphoreAcquireTimeout, TimeUnit.MICROSECONDS);
		
		//region Interlocked
		if (valetAcquired) {
			
			boolean handleAcquired = request.handle();
			
			if (handleAcquired) {
				
				Parcheggiatore valet = valets.poll();
				RestituzioneRequest requestProxy = new RestituzioneRequest(request.getPayload());
				
				requestProxy.addObserver((o, none) -> {
					
					Automobile automobile = ticketAutomobileMap.remove(ticket);
					request.fulfill(automobile);
					
					// Free the parking slot
					int parkingSlot = ticketParkingSlotMap.get(ticket);
					
					int timeSlotStart = ticket.getTimeSlotStart();
					int timeSlotEnd = ticket.getTimeSlotEnd();
					List<Boolean> timeSlotsFree = parkingSlotTimeSlotsMap.get(parkingSlot);
					List<Boolean> timeSlotsFreeAfter;		
				
					if (ticket.getTimeSlotEnd() != PARKING_SLOT_OCCUPIED_INDEFINITELY) {	

						timeSlotsFreeAfter = ListUtils.bitwiseExclusiveUnion(
													ListUtils.makeAdjacencyBitmask(this.reservationTimeSlotCount,
																					timeSlotStart - 1,
																					timeSlotEnd - 1),
													timeSlotsFree);
					}
					else {				
						timeSlotsFreeAfter = ListUtils.makeAdjacencyBitmask(this.reservationTimeSlotCount,
																			0,
																			this.reservationTimeSlotCount - 1);
					}
					
					//region Interlocked
					synchronized (parkingSlotTimeSlotsMap) {
						
						assert parkingSlotTimeSlotsMap.containsKey(parkingSlot);
						
						parkingSlotTimeSlotsMap.put(parkingSlot, timeSlotsFreeAfter);
					}
					//endregion

					freeParkingSlotsSemaphore.release();
					valetsSemaphore.release();
					
					onParcheggioUpdate();
				});
				
				valet.restituisci(requestProxy);
				valets.add(valet);
			}
			else
				valetsSemaphore.release();
			//endregion
		}
	}
	
	/**
	 * @brief Computes a range indicating whether the selected time slot range
	 * 		  is available or not
	 * 
	 * @param timeSlotsToReserve the time slot range to reserve
	 * @param availableTimeSlots the time slots available for reservation
	 * @return the availability range
	 */
	protected List<Boolean> getReservationAvailabilityRange(List<Boolean> timeSlotsToReserve,
															List<Boolean> availableTimeSlots) {
		
		List<Boolean> availabilityRange =
				ListUtils.bitwiseIntersect(timeSlotsToReserve, availableTimeSlots);
		
		return availabilityRange;
	}
	
	/**
	 * @return the parking slot mapped to @see Parcheggio#getAvailabilityRange
	 */
	protected Map.Entry<Integer, List<Boolean>> mapParkingSlotToAvailabilityRange(
														Map.Entry<Integer, List<Boolean>> parkingSlotTimeSlotsEntry,
														List<Boolean> timeSlotsToReserve) {
		
		AbstractMap.SimpleEntry<Integer, List<Boolean>> availabilityRange =
				new AbstractMap.SimpleEntry<Integer, List<Boolean>>(parkingSlotTimeSlotsEntry.getKey(),
																	getReservationAvailabilityRange(
																			parkingSlotTimeSlotsEntry.getValue(),
																			timeSlotsToReserve));
		return availabilityRange;
	}
	
	/**
	 * @brief Reserves a specific time slot range [timeSlotStart, timeSlotEnd]
	 * 
	 * @param timeSlotStart the starting time slot
	 * @param timeSlotEnd the ending time slot
	 * @return an entry/exit ticket if timeSlots have been reserved, null otherwise
	 */
	protected Ticket onReserve(int timeSlotStart, int timeSlotEnd) {
		
		Ticket ticket = null;
		
	
		boolean timeSlotsAreOk = timeSlotStart > 0 && timeSlotStart < timeSlotEnd
												&& timeSlotEnd <= this.reservationTimeSlotCount;
		
		if (timeSlotsAreOk) {
			
			int timeSlotsRequested = timeSlotEnd+1 - timeSlotStart;
			List<Boolean> timeSlotsToReserve = ListUtils.makeAdjacencyBitmask(
														this.reservationTimeSlotCount,
														timeSlotStart - 1, timeSlotEnd - 1);
			//region Interlocked
			synchronized (parkingSlotTimeSlotsMap) {
				
				List<Map.Entry<Integer, List<Boolean>>> parkingSlotCandidates =
						parkingSlotTimeSlotsMap.entrySet().stream()
										.map(x -> mapParkingSlotToAvailabilityRange(x, timeSlotsToReserve))
										.filter(y -> ListUtils.getTrueBitsCount(y.getValue()) == timeSlotsRequested)
										.collect(Collectors.toList());
								
				for (Map.Entry<Integer, List<Boolean>> candidate : parkingSlotCandidates) {
					
					List<Boolean> timeSlots = this.parkingSlotTimeSlotsMap.get(candidate.getKey());
					int freeTimeSlots = ListUtils.getTrueBitsCount(timeSlots);
					int timeSlotsLeft = freeTimeSlots - timeSlotsRequested;
						
					if (timeSlotsLeft == 0) {
						
						boolean acquired = freeParkingSlotsSemaphore.tryAcquire();
						assert acquired == true;
					}
					
					parkingSlotTimeSlotsMap.put(candidate.getKey(), ListUtils.bitwiseExclusiveUnion(timeSlots,
																									timeSlotsToReserve));
					
					ticket = new Ticket(timeSlotStart, timeSlotEnd);
				}
			}
			//endregion
			
			onParcheggioUpdate();
		}
		
		return ticket;
	}
	
	protected void onParcheggioUpdate() {
		
		setChanged();
		notifyObservers(new Snapshot(this));
	}
	
	/**
	 * @brief Posts a @see RitiroRequest to the corresponding message mailbox for later processing
	 * 
	 */
	public void ritira(RitiroRequest request) {
		
		ritiroRequests.add(request);
		
		//region Interlocked
		synchronized (requestsMonitor) {
			requestsMonitor.notify();
		}
		//endregion
	}
	
	/**
	 * @brief Posts a @see RitiroWithTicketRequest to the corresponding message mailbox for later processing
	 * 
	 */
	public void ritiraWithTicket(RitiroWithTicketRequest request) {
		
		ritiroWithTicketRequests.add(request);
		
		//region Interlocked
		synchronized (requestsMonitor) {
			requestsMonitor.notify();
		}
		//endregion
	}
	
	/**
	 * @brief Posts a @see RestituzioneRequest to the corresponding message mailbox for later processing
	 * 
	 */
	public void restituisci(RestituzioneRequest request) {
		
		restituzioneRequests.add(request);
		
		//region Interlocked
		synchronized (requestsMonitor) {
			requestsMonitor.notify();
		}
		//endregion
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getFreeParkingSlots() {
		return freeParkingSlotsSemaphore.availablePermits();
	}
	
	public int getFreeValets() {
		return valetsSemaphore.availablePermits();
	}
	
	public int getRitiriRequestsCount() {
		return ritiroRequests.size();
	}
	
	public int getRestituzioneRequestsCount() {
		return restituzioneRequests.size();
	}
	
	/**
	 * @brief Instantaneous Description of @see Parcheggio
	 *
	 */
	public static class Snapshot {

		protected int parcheggioId;
		protected String parcheggioName;
		
		protected int freeParkingSlots;
		protected int freeValets;
		
		/**
		 * @note It's necessary to leave @see Snapshot default-constructible so as to allow for serialization
		 */
		public Snapshot() {
			
		}
		
		public Snapshot(Parcheggio target) {
			
			parcheggioId = target.id;
			parcheggioName = target.name;
			freeParkingSlots = target.getFreeParkingSlots();
			freeValets = target.getFreeValets();
		}

		public int getParcheggioId() {
			return parcheggioId;
		}

		public void setParcheggioId(int parcheggioId) {
			this.parcheggioId = parcheggioId;
		}

		public String getParcheggioName() {
			return parcheggioName;
		}

		public void setParcheggioName(String parcheggioName) {
			this.parcheggioName = parcheggioName;
		}
		
		public int getFreeParkingSlots() {
			return freeParkingSlots;
		}

		public void setFreeParkingSlots(int freeParkingSlots) {
			this.freeParkingSlots = freeParkingSlots;
		}

		public int getFreeValets() {
			return freeValets;
		}

		public void setFreeValets(int freeValets) {
			this.freeValets = freeValets;
		}
		
	}

}
