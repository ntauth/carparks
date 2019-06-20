package it.unimib.disco.net;

import java.util.List;
import java.util.Map.Entry;

/**
 * Communication class for client and server.
 * 
 * 
 * @param client The client to handle
 */
public class NetMessage {
	
	private NetMessageType type;
	private List<Entry<String, String>> parking; /* In caso di prenotazione per
													un parcheggio, quello da 
													prenotare sarà il primo
													nella lista parking. */
	private int slot;
	
	public NetMessage(NetMessageType type, 
					  List<Entry<String, String>> parking, 
					  int slot) {
		super();
		this.type = type;
		this.parking = parking;
		this.slot = slot;
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
	public List<Entry<String, String>> getParking() {
		return parking;
	}
	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
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
	public void setParking(List<Entry<String, String>> parking) {
		this.parking = parking;
	}
	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}
	/**
	 * @return the parking the user intends to book
	 */
	public Entry<String, String> getParkingToBook()
	{
		Entry<String, String> entry = null;
		if (this.type == NetMessageType.BOOK) {
			entry = parking.get(0);
		}
		return entry;
	}

}
