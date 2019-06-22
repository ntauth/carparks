package it.unimib.disco.net;

import it.unimib.disco.domain.Parcheggio.Snapshot;

/**
 * Communication class for client and server.
 * 
 * 
 * @param client The client to handle
 */
public class ParcheggioNetMessage extends NetMessage{
	
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
	
	/**
	 * @return the type
	 */
	public NetMessageType getType() {
		return type;
	}
	/**
	 * @return the parking
	 */
	public Snapshot getParking() {
		return parking;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(NetMessageType type) {
		this.type = type;
	}
	/**
	 * @param parking the parking to set
	 */
	public void setParking(Snapshot parking) {
		this.parking = parking;
	}


}
