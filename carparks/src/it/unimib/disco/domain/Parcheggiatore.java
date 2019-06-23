package it.unimib.disco.domain;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Parcheggiatore implements Callable<Void> {

	protected Object requestMonitor;
	protected ConcurrentLinkedQueue<RitiroRequest> ritiroRequests;
	protected ConcurrentLinkedQueue<RestituzioneRequest> restituzioneRequests;
	
	public Parcheggiatore() {
	
		requestMonitor = new Object();
		ritiroRequests = new ConcurrentLinkedQueue<>();
		restituzioneRequests = new ConcurrentLinkedQueue<>();
	}
	
	protected void requestWatchdog() throws InterruptedException {
		
		while (true) {
			
			synchronized (requestMonitor) {
				
				int cumulativeSize = ritiroRequests.size() + restituzioneRequests.size();
				
				while (cumulativeSize == 0) {
					
					requestMonitor.wait();
					cumulativeSize = ritiroRequests.size() + restituzioneRequests.size();
				}
			}
			
			RitiroRequest rireq = ritiroRequests.peek();
			
			if (rireq != null) {
				
				onRitira(rireq);
				
				if (rireq.isFulfilled() || rireq.isCanceled())
					ritiroRequests.poll();
			}
			
			RestituzioneRequest rereq = restituzioneRequests.peek();
			
			if (rereq != null) {
				
				onRestituisci(rereq);
				
				if (rereq.isFulfilled() || rereq.isCanceled())
					restituzioneRequests.poll();
			}
		}
	}

	@Override
	public Void call() throws Exception {
		
		requestWatchdog();
		
		return null;
	}
	
	protected void onRitira(RitiroRequest request) {

		request.fulfill(new Ticket());
	}
	
	protected void onRestituisci(RestituzioneRequest request) {
		
		// Fulfill the request with a dummy (null) @see Automobile instance
		// The actual instance will be fulfilled by @see Parcheggio
		request.fulfill(null);
	}
	
	public void ritira(RitiroRequest request) {
		
		ritiroRequests.add(request);
		
		synchronized (requestMonitor) {
			requestMonitor.notify();
		}
	}
	
	public void restituisci(RestituzioneRequest request) {
		
		restituzioneRequests.add(request);
		
		synchronized (requestMonitor) {
			requestMonitor.notify();
		}
	}
	
}
