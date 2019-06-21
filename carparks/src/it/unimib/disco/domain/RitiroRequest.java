package it.unimib.disco.domain;

public class RitiroRequest extends Request<Automobile> {

	public RitiroRequest(Automobile payload) {
		
		super(payload);
	}
	
	public void fulfill(Ticket ticket) {
		
		super.fulfill(ticket);
	}
	
}
