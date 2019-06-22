package it.unimib.disco.net;

import java.util.Optional;

public class SocketClientConnectionEventArgs {

	protected SocketClientConnectionStatus status;
	protected Optional<Exception> exception;
	
	public SocketClientConnectionEventArgs(SocketClientConnectionStatus status, Exception exception) {
		
		this.status = status;
		setException(exception);
	}
	
	public SocketClientConnectionEventArgs(SocketClientConnectionStatus status) {
		
		this(status, null);
	}
	
	public SocketClientConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(SocketClientConnectionStatus status) {
		this.status = status;
	}

	public Optional<Exception> getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = Optional.ofNullable(exception);
	}
	
}
