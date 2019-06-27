package it.unimib.disco.domain;

import java.util.UUID;

public class Ticket {

	protected UUID uuid;
	protected int  reservedTimeSlot;
	
	public Ticket() {
		this(UUID.randomUUID());
	}
	
	public Ticket(UUID uuid) {
		
		this(uuid, -1);
	}
	
	public Ticket(int reservedTimeSlot) {
		
		this(UUID.randomUUID(), reservedTimeSlot);
	}
	
	public Ticket(UUID uuid, int reservedTimeSlot) {
		
		this.uuid = uuid;
		this.reservedTimeSlot = reservedTimeSlot;
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public int getReservedTimeSlot() {
		return reservedTimeSlot;
	}

	public void setReservedTimeSlot(int reservedTimeSlot) {
		this.reservedTimeSlot = reservedTimeSlot;
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
