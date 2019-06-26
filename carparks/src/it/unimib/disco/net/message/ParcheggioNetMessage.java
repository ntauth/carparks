package it.unimib.disco.net.message;

import it.unimib.disco.domain.Parcheggio.Snapshot;

import java.util.UUID;

import it.unimib.disco.domain.Ticket;

/**
 * Communication class for client and server.
 * 
 * 
 * @param client The client to handle
 */
public class ParcheggioNetMessage extends NetMessage{
	
	
	public static final ParcheggioNetMessage EMPTY = new ParcheggioNetMessage(NetMessageType.NONE, null);
	
	private Snapshot parking; /* In caso di prenotazione per
													un parcheggio, quello da 
													prenotare sarà il primo
													nella lista parking. */
	
	private String tracer;
	
	public ParcheggioNetMessage()
	{
		//Per Jackson
	}
	
	public ParcheggioNetMessage(NetMessageType type, 
								Snapshot parking) 
	{
		super(type);
		this.parking = parking;
		this.tracer = UUID.randomUUID().toString();
	}
	
	public ParcheggioNetMessage(NetMessageType type, 
								Ticket ticket,
								int timeSlot)
	{
		super(type, ticket, timeSlot);
		this.tracer = UUID.randomUUID().toString();
	}
	
	public ParcheggioNetMessage(NetMessageType type, 
								Snapshot parking,
								int timeSlot) 
	{
		super(type, timeSlot);
		this.parking = parking;
		this.tracer = UUID.randomUUID().toString();
	}
	
	/**
	 * @return the parking
	 */
	public Snapshot getParking() {
		return parking;
	}

	/**
	 * @param parking the parking to set
	 */
	public void setParking(Snapshot parking) {
		this.parking = parking;
	}

	/**
	 * @return the tracer
	 */
	public String getTracer() {
		return tracer;
	}

	/**
	 * @param tracer the tracer to set
	 */
	public void setTracer(String tracer) {
		this.tracer = tracer;
	}
	
}
