package it.unimib.disco.entities;

import java.util.concurrent.Callable;

public class Automobilista implements Callable<Void> {

	protected Automobile automobile;

	public Automobilista(Automobile automobile) {
		
		this.automobile = automobile;
	}
	
	
	
	@Override
	public Void call() throws Exception {
		
		return null;
	}
	
}
