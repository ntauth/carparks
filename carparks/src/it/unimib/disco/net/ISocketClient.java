package it.unimib.disco.net;

import java.util.function.Consumer;

public interface ISocketClient {

	public void connect(String ip, int port, Consumer<SocketClientConnectionEventArgs> connectionStatusChangedDelegate);
	
	default void connect(String ip, int port) {		
		connect(ip, port, null);
	}
	
	public Object readObject();
	
	public void writeObject(Object obj);
	
	public SocketClientConnectionStatus getConnectionStatus();
}
