package it.unimib.disco.domain;

public class RitiroWithTicketRequest extends RitiroRequest {

	protected Ticket ticket;

	public RitiroWithTicketRequest(Automobile payload, Ticket ticket) {
		
		super(payload);
		this.ticket = ticket;
	}
	
	public Ticket getPayloadTicket() {
		return ticket;
	}
	
	public void setPayloadTicket(Ticket ticket) {
		this.ticket = ticket;
	}
}
