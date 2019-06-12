package it.unimib.disco;

import it.unimib.disco.entities.Automobile;
import it.unimib.disco.entities.Ticket;

public class RitiroRequest extends Request<Automobile> {

	public RitiroRequest(Automobile payload) {
		
		super(payload);
	}
	
	public void fulfill(Ticket ticket) {
		
		super.fulfill(ticket);
	}
	
}
