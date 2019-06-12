package it.unimib.disco;

import it.unimib.disco.entities.Automobile;
import it.unimib.disco.entities.Ticket;

public class RestituzioneRequest extends Request<Ticket> {

	public RestituzioneRequest(Ticket payload) {
		
		super(payload);
	}
	
	public void fulfill(Automobile automobile) {
		
		super.fulfill(automobile);
	}
	
}
