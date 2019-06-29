package it.unimib.disco.net.message;

import it.unimib.disco.domain.Parcheggio.Snapshot;

import java.util.UUID;

import it.unimib.disco.domain.Ticket;

/**
 * This class represents the base structure of the messages exchanged
 * between @see ParcheggioSocketClient and @see SocketServer
 *
 */
public class ParcheggioNetMessage extends NetMessage {
	
	public static final ParcheggioNetMessage EMPTY = new ParcheggioNetMessage(NetMessageType.NONE, null);
	
	private Snapshot parking; 
	private String tracer;
	
	public ParcheggioNetMessage() {
		
	}
	
	public ParcheggioNetMessage(NetMessageType type, Snapshot parking) {
		
		super(type);
		this.parking = parking;
		this.tracer = UUID.randomUUID().toString();
	}
	
	public ParcheggioNetMessage(NetMessageType type, Ticket ticket, int timeSlot) {
		
		super(type, ticket, timeSlot);
		this.tracer = UUID.randomUUID().toString();
	}
	
	public ParcheggioNetMessage(NetMessageType type, Snapshot parking, int timeSlot) {
		
		super(type, timeSlot);
		this.parking = parking;
		this.tracer = UUID.randomUUID().toString();
	}
	
	public Snapshot getParking() {
		return parking;
	}

	public void setParking(Snapshot parking) {
		this.parking = parking;
	}

	public String getTracer() {
		return tracer;
	}

	public void setTracer(String tracer) {
		this.tracer = tracer;
	}
	
}
