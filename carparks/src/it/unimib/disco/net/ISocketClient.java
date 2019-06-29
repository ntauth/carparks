package it.unimib.disco.net;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ISocketClient {

	public void connect(String ip, int port, Consumer<SocketClientConnectionEventArgs> connectionStatusChangedDelegate);
	
	default void connect(String ip, int port) {		
		connect(ip, port, null);
	}
	
	public Object readObject(Class<?> archetype) throws IOException, ClassNotFoundException;
	public CompletableFuture<Object> readObjectAsync(Class<?> archetype);
	public void writeObject(Object obj) throws IOException;
	
	public SocketClientConnectionStatus getConnectionStatus();
	
}
