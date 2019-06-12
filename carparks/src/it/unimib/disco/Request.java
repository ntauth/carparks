package it.unimib.disco;

import java.util.Observable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import it.unimib.disco.utils.SynchroPrimitives;

public abstract class Request<P> extends Observable {
	
	protected P payload;
	protected boolean canceled;
	protected boolean beingHandled;
	protected boolean fulfilled;
	protected Lock lock;
	
	public Request(P payload) {
		
		setPayload(payload);
		canceled = false;
		beingHandled = false;
		fulfilled = false;
		lock = new ReentrantLock(true);
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public boolean isBeingHandled() {
		return beingHandled;
	}
	
	public boolean isFulfilled() {
		return fulfilled;
	}
	
	public void setPayload(P payload) {
		this.payload = payload;
	}
	
	public P getPayload() {
		return payload;
	}
	
	public void fulfill(Object object) {
		
		lock.lock();
		
		setChanged();
		notifyObservers(object);
		fulfilled = true;
		
		lock.unlock();
	}
	
	public boolean handle() {
		
		boolean prev = false;
		
		lock.lock();
		
		if (!canceled)
			prev = SynchroPrimitives.testAndSet(beingHandled);
		
		lock.unlock();
		
		return prev;
	}
	
	public boolean cancel() {
		
		boolean prev = false;
		
		lock.lock();
		
		if (!beingHandled && !fulfilled)
			prev = SynchroPrimitives.testAndSet(canceled);
		
		lock.unlock();
		
		return prev;
	}
}
