package it.unimib.disco.net;


import java.util.List;

import it.unimib.disco.domain.Parcheggio.Snapshot;

/**
 * Communication class for client and server.
 * 
 * 
 * @param client The client to handle
 */
public class ParcheggioNetMessage extends NetMessage{
	
	private List<Snapshot> parking; /* In caso di prenotazione per
													un parcheggio, quello da 
													prenotare sarà il primo
													nella lista parking. */
	
	public ParcheggioNetMessage(NetMessageType type, 
					  			List<Snapshot> parking) 
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
	public List<Snapshot> getParking() {
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
	public void setParking(List<Snapshot> parking) {
		this.parking = parking;
	}
	/**
	 * @return the parking the user intends to book
	 */
	public Snapshot getParkingToBook()
	{
		Snapshot entry = null;
		if (this.type == NetMessageType.BOOK) {
			entry = parking.get(0);
		}
		return entry;
	}

}
