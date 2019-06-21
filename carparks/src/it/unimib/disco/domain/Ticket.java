package it.unimib.disco.domain;

import java.util.UUID;

public class Ticket {

	protected UUID uuid;
	
	public Ticket() {
		this(UUID.randomUUID());
	}
	
	public Ticket(UUID uuid) {
		this.uuid = uuid;
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
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
