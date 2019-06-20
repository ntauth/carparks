package it.unimib.disco.net;

import java.util.List;

import it.unimib.disco.Parcheggio;

public class ApplicationContext {

	protected List<Parcheggio> parkSlots;

	public ApplicationContext(List<Parcheggio> parcheggi) {
		
		this.parkSlots = parcheggi;
	}

	public List<Parcheggio> getParcheggi() {
		return parkSlots;
	}
}
