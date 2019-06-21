package it.unimib.disco.domain;

public class RestituzioneRequest extends Request<Ticket> {

	public RestituzioneRequest(Ticket payload) {
		
		super(payload);
	}
	
	public void fulfill(Automobile automobile) {
		
		super.fulfill(automobile);
	}
	
}
