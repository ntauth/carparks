package it.unimib.disco.net.message;

import it.unimib.disco.domain.Parcheggio.Snapshot;
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
	
	public ParcheggioNetMessage()
	{
		//Per Jackson
	}
	
	public ParcheggioNetMessage(NetMessageType type, 
								Snapshot parking) 
	{
		super(type);
		this.parking = parking;
	}
	
	public ParcheggioNetMessage(NetMessageType type, 
								Ticket ticket,
								int timeSlot)
	{
		super(type, ticket, timeSlot);
	}
	
	public ParcheggioNetMessage(NetMessageType type, 
								Snapshot parking,
								int timeSlot) 
	{
		super(type, timeSlot);
		this.parking = parking;
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


}
