package it.unimib.disco.entities;

import java.util.UUID;

public class Ticket {

	protected UUID uuid;
	
	public Ticket() {
		this(UUID.randomUUID());
	}
	
	public Ticket(UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public int hashCode() {
		
		return uuid.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		
		if (other == null || this.getClass() != other.getClass())
			return false;
		
		Ticket otherAsTicket = (Ticket) other;
		
		return uuid.equals(otherAsTicket.uuid);
	}
}
