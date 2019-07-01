package it.unimib.disco.domain;

import java.util.UUID;

public class Ticket {

	protected UUID uuid;
	protected int timeSlotStart;
	protected int timeSlotEnd;
	
	public Ticket() {
		this(UUID.randomUUID());
	}
	
	public Ticket(UUID uuid) {
		
		this(uuid, 0, Integer.MAX_VALUE);
	}
	
	public Ticket(int timeSlotStart, int timeSlotEnd) {
		
		this(UUID.randomUUID(), timeSlotStart, timeSlotEnd);
	}
	
	public Ticket(UUID uuid, int timeSlotStart, int timeSlotEnd) {
		
		this.uuid = uuid;
		this.timeSlotStart = timeSlotStart;
		this.timeSlotEnd = timeSlotEnd;
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public int getTimeSlotStart() {
		return timeSlotStart;
	}

	public void setTimeSlotStart(int timeSlotStart) {
		this.timeSlotStart = timeSlotStart;
	}

	public int getTimeSlotEnd() {
		return timeSlotEnd;
	}

	public void setTimeSlotEnd(int timeSlotEnd) {
		this.timeSlotEnd = timeSlotEnd;
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
