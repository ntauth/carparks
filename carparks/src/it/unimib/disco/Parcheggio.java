package it.unimib.disco;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import it.unimib.disco.entities.Automobile;
import it.unimib.disco.entities.Automobilista;
import it.unimib.disco.entities.Parcheggiatore;
import it.unimib.disco.entities.Ticket;

public class Parcheggio implements Callable<Void> {
	
	protected Semaphore freeParkingSlotsSemaphore;
	
	protected Queue<Parcheggiatore> parcheggiatori;
	protected Semaphore parcheggiatoriSemaphore;
	
	protected Map<Ticket, Automobile>    ticketAutoMap;
	protected Map<Ticket, Automobilista> ticketAutomobilistaMap;
	
	protected ConcurrentLinkedQueue<RitiroRequest> ritiroRequests;
	protected ConcurrentLinkedQueue<RestituzioneRequest> restituzioneRequests;
	
	public Parcheggio(int freeParkingSlots, List<Parcheggiatore> parcheggiatori) {
		
		this.freeParkingSlotsSemaphore = new Semaphore(freeParkingSlots, true);
		this.parcheggiatori = new LinkedList<>(); // Dependency injection
		
		for (Parcheggiatore p : parcheggiatori)
			this.parcheggiatori.add(p);
	}
	
	@Override
	public Void call() throws Exception {
		
		requestWatchdog();
		
		return null;
	}
	
	protected void requestWatchdog() throws InterruptedException {
		
		while (true) {
			
			RitiroRequest rireq = ritiroRequests.poll();
			
			if (rireq != null)
				onRitira(rireq);
			
			RestituzioneRequest rereq = restituzioneRequests.poll();
			
			if (rereq != null)
				onRestituisci(rereq);
		}
	}
	
	protected void onRitira(RitiroRequest request) throws InterruptedException {
		
//		parcheggiatoriSemaphore.acquire();
//
//		//region Interlocked
//		Ticket  ticket = null;
//		boolean locked = freeParkingSlotsLock.tryLock(freeParkingSlotFetchTimeout, TimeUnit.SECONDS);
//		
//		if (locked) {
//			
//			int freeSlotIndex = -1;
//			
//			for (int i = 0; i < freeParkingSlots.length; i++) {
//				
//				if (freeParkingSlots[i]) {
//					
//					freeSlotIndex = i;
//					break;
//				}
//			}
//			
//			if (freeSlotIndex > 0) {
//				
//				freeParkingSlots[freeSlotIndex] = false;
//				
//				// Create the ticket
//				ticket = new Ticket();
//			}
//		}
//		//endregion
//		
//		parcheggiatoriSemaphore.release();
//		
//		return ticket;
	}
	
//	public Optional<Ticket> tryRitira(Automobile auto) {
//	
//		Optional<Ticket> nullableTicket;
//		
//		try {
//			nullableTicket = Optional.of(ritira(auto));
//		}
//		catch (InterruptedException e) {
//			nullableTicket = Optional.empty();
//		}
//		
//		return nullableTicket;
//	}
	
	protected void onRestituisci(RestituzioneRequest request) {
		
	}
	
	public void ritira(RitiroRequest request) {
		
		ritiroRequests.add(request);
	}
	
	public void restituisci(RestituzioneRequest request) {
		
		restituzioneRequests.add(request);
	}
}
