package it.unimib.disco;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import it.unimib.disco.entities.Automobile;
import it.unimib.disco.entities.Parcheggiatore;
import it.unimib.disco.entities.Ticket;

public class Parcheggio implements Callable<Void> {
	
	protected static final long FPS_SEM_ACQUIRE_TIMEOUT_INITIAL = 500L;
	protected static final long PARK_SEM_ACQUIRE_TIMEOUT_INITIAL = 500L;
	
	protected Semaphore freeParkingSlotsSemaphore;
	protected long fpsSemAcquireTimeout;
	
	protected Queue<Parcheggiatore> parcheggiatori;
	protected Semaphore parcheggiatoriSemaphore;
	protected long parkSemAcquireTimeout;
	
	protected Map<Ticket, Automobile> ticketAutoMap;
	
	protected ConcurrentLinkedQueue<RitiroRequest> ritiroRequests;
	protected ConcurrentLinkedQueue<RestituzioneRequest> restituzioneRequests;
	
	public Parcheggio(int freeParkingSlots, List<Parcheggiatore> parcheggiatori) {
		
		this.freeParkingSlotsSemaphore = new Semaphore(freeParkingSlots, true);
		this.fpsSemAcquireTimeout = FPS_SEM_ACQUIRE_TIMEOUT_INITIAL;
		
		this.parcheggiatoriSemaphore = new Semaphore(parcheggiatori.size(), true);
		this.parcheggiatori = new LinkedList<>();
		this.parkSemAcquireTimeout = PARK_SEM_ACQUIRE_TIMEOUT_INITIAL;
		
		// Dependency Injection
		for (Parcheggiatore p : parcheggiatori)
			this.parcheggiatori.add(p);
		
		this.ticketAutoMap = new HashMap<>();
		
		this.ritiroRequests = new ConcurrentLinkedQueue<>();
		this.restituzioneRequests = new ConcurrentLinkedQueue<>();
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
			
			boolean parkAcquired = parcheggiatoriSemaphore.tryAcquire(parkSemAcquireTimeout, TimeUnit.MICROSECONDS);
			
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
			
				parcheggiatoriSemaphore.release();
			}
			//endregion
		}
	}
	
	protected void onRestituisci(RestituzioneRequest request) throws InterruptedException {
		
		boolean parkAcquired = parcheggiatoriSemaphore.tryAcquire(parkSemAcquireTimeout, TimeUnit.MILLISECONDS);
		
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
			
			parcheggiatoriSemaphore.release();
			//endregion
		}
	}
	
	public void ritira(RitiroRequest request) {	
		ritiroRequests.add(request);
	}
	
	public void restituisci(RestituzioneRequest request) {
		restituzioneRequests.add(request);
	}
	
	public int getFreeParkingSlots() {
		return freeParkingSlotsSemaphore.availablePermits();
	}
	
	public int getFreeParcheggiatori() {
		return parcheggiatoriSemaphore.availablePermits();
	}
	
	public int getRitiriRequestsCount() {
		return ritiroRequests.size();
	}
	
	public int getRestituzioneRequestsCount() {
		return restituzioneRequests.size();
	}
	
}
