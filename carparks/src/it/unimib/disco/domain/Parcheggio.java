package it.unimib.disco.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
	
	protected ConcurrentLinkedQueue<RitiroRequest> ritiroRequests;
	protected ConcurrentLinkedQueue<RestituzioneRequest> restituzioneRequests;
	
	public Parcheggio(int freeParkingSlots, List<Parcheggiatore> valets) {
		
		this(UUID.randomUUID().hashCode(), "", freeParkingSlots, valets);
	}
	
	public Parcheggio(int id, String name, int freeParkingSlots, List<Parcheggiatore> valets) {
		
		// Create the time slots and initialize them all to true (free)
		this.freeReservationTimeSlots = new HashMap<>();
		
		for (int i = 1; i <= RESERVATION_TIME_SLOT_COUNT; i++)
			freeReservationTimeSlots.put(i, true);
		
		this.freeParkingSlotsSemaphore = new Semaphore(freeParkingSlots, true);
		this.fpsSemAcquireTimeout = PARKSLOT_SEMAPHORE_ACQUIRE_TIMEOUT;
		
		this.valetsSemaphore = new Semaphore(valets.size(), true);
		this.valets = new LinkedList<>();
		this.parkSemAcquireTimeout = VALETS_SEMAPHORE_ACQUIRE_TIMEOUT;
		
		// Dependency Injection
		for (Parcheggiatore p : valets)
			this.valets.add(p);
		
		this.ticketAutoMap = new HashMap<>();
		
		this.ritiroRequests = new ConcurrentLinkedQueue<>();
		this.restituzioneRequests = new ConcurrentLinkedQueue<>();
		
		// Update the observers, if any
		onParcheggioUpdate();
	}
	
	@Override
	public Void call() throws Exception {
		
		requestWatchdog();
		
		return null;
	}
	
	protected void requestWatchdog() throws InterruptedException {
		
		while (true) {
			
			RitiroRequest rireq = ritiroRequests.peek();
			
			if (rireq != null) {
				
				onRitira(rireq);
				
				if (rireq.isFulfilled() || rireq.isCanceled())
					ritiroRequests.poll();
			}
			
			RestituzioneRequest rereq = restituzioneRequests.peek();
			
			if (rereq != null) {
				
				onRestituisci(rereq);
				
				if (rereq.isFulfilled() || rereq.isCanceled())
					restituzioneRequests.poll();
			}
		}
	}
	
	protected void onRitira(RitiroRequest request) throws InterruptedException {
		
		boolean fpsAcquired = freeParkingSlotsSemaphore.tryAcquire(fpsSemAcquireTimeout, TimeUnit.MICROSECONDS);

		if (fpsAcquired) {
			
			boolean parkAcquired = valetsSemaphore.tryAcquire(parkSemAcquireTimeout, TimeUnit.MICROSECONDS);
			
			//region Interlocked
			if (parkAcquired) {
				
				boolean handleAcquired = request.handle();
				
				if (handleAcquired) {
					
					// @todo: Invoke Parcheggiatore#ritira
					// <---->
					
					Ticket ticket = new Ticket();
					ticketAutoMap.put(ticket, request.getPayload());
					
					request.fulfill(ticket);
				}
			
				valetsSemaphore.release();
			}
			//endregion
		}
	}
	
	protected void onRestituisci(RestituzioneRequest request) throws InterruptedException {
		
		boolean parkAcquired = valetsSemaphore.tryAcquire(parkSemAcquireTimeout, TimeUnit.MILLISECONDS);
		
		if (parkAcquired) {
			
			//region Interlocked
			Ticket ticket = request.getPayload();
			
			boolean handleAcquired = request.handle();
			
			if (handleAcquired) {
				
				// @todo: Invoke Parcheggiatore#restituisci
				// <---->
				
				Automobile automobile = ticketAutoMap.remove(ticket);
				request.fulfill(automobile);
				
				// Release the parking slot
				freeParkingSlotsSemaphore.release();
			}
			
			valetsSemaphore.release();
			//endregion
		}
	}
	
	protected void onParcheggioUpdate() {
		
		setChanged();
		notifyObservers(new Snapshot(this));
	}
	
	public void ritira(RitiroRequest request) {	
		ritiroRequests.add(request);
	}
	
	public void restituisci(RestituzioneRequest request) {
		restituzioneRequests.add(request);
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
