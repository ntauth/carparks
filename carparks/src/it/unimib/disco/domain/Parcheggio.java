package it.unimib.disco.domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unimib.disco.net.ParcheggioSocketClient;
import it.unimib.disco.net.message.ParcheggioNetMessage;
import it.unimib.disco.net.serialization.JsonSerializationPolicy;

public class Parcheggio extends Observable implements Callable<Void> {
	
	public static final int RESERVATION_TIME_SLOT_COUNT = 48;
	
	protected static final long PARKSLOT_SEMAPHORE_ACQUIRE_TIMEOUT = 500L;
	protected static final long VALETS_SEMAPHORE_ACQUIRE_TIMEOUT = 500L;
	
	protected int id;
	protected String name;
	
	protected Map<Integer, Boolean> freeReservationTimeSlots;
	
	protected Semaphore freeParkingSlotsSemaphore;
	protected long fpsSemAcquireTimeout;
	
	protected Queue<Parcheggiatore> valets;
	protected Semaphore valetsSemaphore;
	protected long parkSemAcquireTimeout;
	
	protected Map<Ticket, Automobile> ticketAutoMap;
	
	protected Object requestMonitor;
	protected ConcurrentLinkedQueue<RitiroRequest> ritiroRequests;
	protected ConcurrentLinkedQueue<RestituzioneRequest> restituzioneRequests;
	
	protected ExecutorService threadPool;
	protected ParcheggioSocketClient socket;

	public Parcheggio(int freeParkingSlots, List<Parcheggiatore> valets) {
		
		this(UUID.randomUUID().hashCode(), "", freeParkingSlots, valets);
	}
	
	public Parcheggio(int id, String name, int freeParkingSlots, List<Parcheggiatore> valets) {
		
		this.id = id;
		this.name = name;
		
		// Create the time slots and initialize them all to true (free)
		this.freeReservationTimeSlots = new HashMap<>();
		
		for (int i = 1; i <= RESERVATION_TIME_SLOT_COUNT; i++)
			freeReservationTimeSlots.put(i, true);
		
		this.freeParkingSlotsSemaphore = new Semaphore(freeParkingSlots, true);
		this.fpsSemAcquireTimeout = PARKSLOT_SEMAPHORE_ACQUIRE_TIMEOUT;
		
		this.valetsSemaphore = new Semaphore(valets.size(), true);
		this.valets = new LinkedList<>();
		this.parkSemAcquireTimeout = VALETS_SEMAPHORE_ACQUIRE_TIMEOUT;
		
		// Inject the valets dependency
		for (Parcheggiatore v : valets)
			this.valets.add(v);
		
		this.ticketAutoMap = new HashMap<>();
		
		this.requestMonitor = new Object();
		this.ritiroRequests = new ConcurrentLinkedQueue<>();
		this.restituzioneRequests = new ConcurrentLinkedQueue<>();
		
		// Thread pool for valets and net watchdog
		this.threadPool = Executors.newFixedThreadPool(valets.size() + 1);
		this.socket = new ParcheggioSocketClient(new JsonSerializationPolicy());
	}
	
	protected CompletableFuture<Object> getNextNetMessageAsync() {
		
		CompletableFuture<Object> message = socket.readObjectAsync(ParcheggioNetMessage.class);
		
		return message;
	}
	
	protected void processNextNetMessage(ParcheggioNetMessage msg) throws IOException {
		
		System.out.println(msg.getType());
		
		switch (msg.getType()) {
				
			case RESERVE_TIME_SLOT:
				boolean reserved = onReserve(msg.getSlot());
				
				if (!reserved)
					msg.setSlot(-1);
				
				socket.sendSnapshot(new Snapshot(this));
				System.out.println("Reserved: " + reserved);
				break;
				
			default:
				break;
		}
	}
	
	public void connectToPlatform(String ip, int port) {
	
		socket.connect(ip, port);
		
		// Add the platform as a remote observer
		addObserver((o, s) -> {
			
			try {
				socket.sendSnapshot((Parcheggio.Snapshot) s);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		// Update the observers, if any
		onParcheggioUpdate();
	}
	
	@Override
	public Void call() throws Exception {
		
		// Launch valets
		for (Parcheggiatore v : valets)
			threadPool.submit(v);
		
		// Launch the net watchdog
		threadPool.submit(() -> netRequestWatchdog());
		
		// Launch the local watchdog
		requestWatchdog();
		
		return null;
	}
	
	protected void netRequestWatchdog() {
	
		while (true) {
			
			CompletableFuture<Object> message_ = getNextNetMessageAsync();
			
			try {
				ParcheggioNetMessage message = (ParcheggioNetMessage) message_.get();
				
				if (message != null)
					processNextNetMessage(message);
			}
			catch (Exception e) {
				
				e.printStackTrace();
				break;
			}
		}
	}
	
	protected void requestWatchdog() throws InterruptedException {
		
		while (true) {		
			
			synchronized (requestMonitor) {
				
				int cumulativeSize = ritiroRequests.size() + restituzioneRequests.size();
				
				while (cumulativeSize == 0) {
					
					requestMonitor.wait();
					cumulativeSize = ritiroRequests.size() + restituzioneRequests.size();
				}
			}
			
			RitiroRequest rireq = ritiroRequests.poll();
			
			if (rireq != null) {
				
				if (!rireq.isBeingHandled()) {
					
					onRitira(rireq);
				}
				else if (!rireq.isFulfilled())
					ritiroRequests.add(rireq);
			}
			
			RestituzioneRequest rereq = restituzioneRequests.poll();
			
			if (rereq != null) {
				
				if (!rereq.isBeingHandled()) {
					
						onRestituisci(rereq);
				}
				else if (!rereq.isFulfilled())
					restituzioneRequests.add(rereq);
			}
		}
	}
	
	protected void onRitira(RitiroRequest request) throws InterruptedException {
		
		boolean slotAcquired = freeParkingSlotsSemaphore.tryAcquire(fpsSemAcquireTimeout, TimeUnit.MICROSECONDS);

		if (slotAcquired) {
			
			boolean valetAcquired = valetsSemaphore.tryAcquire(parkSemAcquireTimeout, TimeUnit.MICROSECONDS);
			
			//region Interlocked
			if (valetAcquired) {
				
				boolean handleAcquired = request.handle();
				
				if (handleAcquired) {
					
					Parcheggiatore valet = valets.poll();
					RitiroRequest requestProxy = new RitiroRequest(request.getPayload());
					
					requestProxy.addObserver((o, ticket) -> {
						
						ticketAutoMap.put((Ticket) ticket, request.getPayload());
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
	}
	
	protected void onRestituisci(RestituzioneRequest request) throws InterruptedException {
		
		boolean valetAcquired = valetsSemaphore.tryAcquire(parkSemAcquireTimeout, TimeUnit.MILLISECONDS);
		
		if (valetAcquired) {
			
			//region Interlocked
			Ticket ticket = request.getPayload();
			
			boolean handleAcquired = request.handle();
			
			if (handleAcquired) {
				
				Parcheggiatore valet = valets.poll();
				RestituzioneRequest requestProxy = new RestituzioneRequest(request.getPayload());
				requestProxy.addObserver((o, _null) -> {
					
					Automobile automobile = ticketAutoMap.remove(ticket);
					request.fulfill(automobile);
					
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
	
	protected boolean onReserve(int timeSlot) {
		
		boolean reserved = false;
		
		synchronized (freeReservationTimeSlots) {
			
			if (freeReservationTimeSlots.containsKey(timeSlot)) {
				
				if (freeReservationTimeSlots.get(timeSlot) == true) {
					
					LocalDateTime dt = LocalDateTime.now();
					int fixup = dt.getMinute() >= 30 ? 1 : 0;
					int currentTimeSlot = 1 + (freeReservationTimeSlots.size() / 24) * (dt.getHour()) + fixup;
					
					for (int i = currentTimeSlot; i != timeSlot; i = (i + 1) % freeReservationTimeSlots.size()) {
					
						if (i == 0)
							i++;
						
						freeReservationTimeSlots.put(i, false);
					}
					
					reserved = true;
				}
			}
		}
		
		return reserved;
	}
	
	protected void onParcheggioUpdate() {
		
		setChanged();
		notifyObservers(new Snapshot(this));
	}
	
	public void ritira(RitiroRequest request) {
		
		ritiroRequests.add(request);
		
		synchronized (requestMonitor) {
			requestMonitor.notify();
		}
	}
	
	public void restituisci(RestituzioneRequest request) {
		
		restituzioneRequests.add(request);
		
		synchronized (requestMonitor) {
			requestMonitor.notify();
		}
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
	
	public boolean isTimeSlotFree(int timeSlot) {
		
		if (freeReservationTimeSlots.containsKey(timeSlot))
			return freeReservationTimeSlots.get(timeSlot);
		
		throw new IllegalArgumentException("Invalid time slot");
	}
	
	/**
	 * @brief Instantaneous Description of @see Parcheggio
	 *
	 */
	public static class Snapshot {

		protected int parcheggioId;
		protected String parcheggioName;
		
		protected Map<Integer, Boolean> freeReservationTimeSlots;
		protected int freeParkingSlots;
		protected int freeValets;
		
		public Snapshot() {
			
		}
		
		public Snapshot(Parcheggio target) {
			
			parcheggioId = target.id;
			parcheggioName = target.name;
			freeReservationTimeSlots = target.freeReservationTimeSlots;
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

		public Map<Integer, Boolean> getFreeReservationTimeSlots() {
			return freeReservationTimeSlots;
		}

		public void setFreeReservationTimeSlots(Map<Integer, Boolean> freeReservationTimeSlots) {
			this.freeReservationTimeSlots = freeReservationTimeSlots;
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

		public Map<Integer, Boolean> getFreeTimeSlots() {
			return freeReservationTimeSlots;
		}
		
	}

}
