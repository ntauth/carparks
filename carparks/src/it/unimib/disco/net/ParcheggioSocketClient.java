package it.unimib.disco.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import it.unimib.disco.domain.Parcheggio;

public class ParcheggioSocketClient extends SocketClientBase {

	protected InputStream istream;
	protected OutputStream ostream;
	protected Scanner reader;
	protected PrintWriter writer;
	
	public ParcheggioSocketClient() {
		
		super();
	}
	
	public ParcheggioSocketClient(ISerializationPolicy serializationPolicy) {
		
		super(serializationPolicy);
	}
	
	public void sendSnapshot(Parcheggio.Snapshot snapshot) throws IOException {
		
		writeObject(snapshot);
	}
	
	@Override
	public void connect(String ip, int port, Consumer<SocketClientConnectionEventArgs> connectionStatusChangedDelegate) {
		
		super.connect(ip, port, connectionStatusChangedDelegate);
		
		if (connectionStatus == SocketClientConnectionStatus.CONNECTED) {
			
			reader = new Scanner(istream);
			writer = new PrintWriter(ostream);
		}
	}
	
	@Override
	public Object readObject(Class<?> archetype) throws IOException, ClassNotFoundException {
		
		assert reader != null;
		
		return serializationPolicy.deserialize(reader.nextLine().getBytes(), archetype);
	}

	@Override
	public CompletableFuture<Object> readObjectAsync(Class<?> archetype) {
		
		CompletableFuture<Object> promise = CompletableFuture.supplyAsync(() -> {
			
			try {
				return readObject(archetype);
			}
			catch (Exception e) {
				return e;
			}
		});
		
		return promise;
	}
	
	@Override
	public void writeObject(Object obj) throws IOException {
		
		assert writer != null;
		
		writer.println(new String(serializationPolicy.serialize(obj)));
	}
	
}
