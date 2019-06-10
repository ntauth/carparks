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
}
