package it.unimib.disco;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import it.unimib.disco.entities.Automobile;
import it.unimib.disco.entities.Automobilista;
import it.unimib.disco.entities.Parcheggiatore;
import it.unimib.disco.entities.Ticket;

public class Parcheggio {
	
	protected boolean[] freeParkingSlots;
	protected Lock      freeParkingSlotsLock;
	protected long		freeParkingSlotFetchTimeout = 5;
	
	protected Queue<Parcheggiatore> freeParcheggiatori;
	protected Queue<Parcheggiatore> busyParcheggiatori;
	protected Semaphore parcheggiatoriSemaphore;
	
	protected Map<Ticket, Automobile>    ticketAutoMap;
	protected Map<Ticket, Automobilista> ticketAutomobilistaMap;
	
	public Ticket ritira(Automobile auto) throws InterruptedException {
		
		parcheggiatoriSemaphore.acquire();
		
		//region Interlocked
		Ticket  ticket = null;
		boolean locked = freeParkingSlotsLock.tryLock(freeParkingSlotFetchTimeout, TimeUnit.SECONDS);
		
		if (locked) {
			
			int freeSlotIndex = -1;
			
			for (int i = 0; i < freeParkingSlots.length; i++) {
				
				if (freeParkingSlots[i]) {
					
					freeSlotIndex = i;
					break;
				}
			}
			
			if (freeSlotIndex > 0) {
				
				freeParkingSlots[freeSlotIndex] = false;
				
				// Create the ticket
				ticket = new Ticket();
			}
		}
		//endregion
		
		parcheggiatoriSemaphore.release();
		
		return ticket;
	}
	
	public Optional<Ticket> tryRitira(Automobile auto) {
	
		Optional<Ticket> nullableTicket;
		
		try {
			nullableTicket = Optional.of(ritira(auto));
		}
		catch (InterruptedException e) {
			nullableTicket = Optional.empty();
		}
		
		return nullableTicket;
	}
	
	public void restituisci(Ticket ticket) {
		
	}
}
