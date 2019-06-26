package it.unimib.disco.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.function.Consumer;

import it.unimib.disco.domain.Parcheggio;
import it.unimib.disco.net.message.NetMessageType;
import it.unimib.disco.net.message.ParcheggioNetMessage;
import it.unimib.disco.net.serialization.ISerializationPolicy;

public class ParcheggioSocketClient extends SocketClientBase {

	protected Scanner reader;
	protected PrintWriter writer;
	
	public ParcheggioSocketClient() {
		
		super();
	}
	
	public ParcheggioSocketClient(ISerializationPolicy serializationPolicy) {
		
		super(serializationPolicy);
	}
	
	@Override
	public void connect(String ip, int port, Consumer<SocketClientConnectionEventArgs> connectionStatusChangedDelegate) {
		
		super.connect(ip, port, connectionStatusChangedDelegate);
		
		if (connectionStatus == SocketClientConnectionStatus.CONNECTED) {
			
			reader = new Scanner(istream);
			writer = new PrintWriter(ostream, true);
			
			// onReady
			onConnectStatusChanged(new SocketClientConnectionEventArgs(SocketClientConnectionStatus.READY));
		}
	}
	
	@Override
	public Object readObject(Class<?> archetype) throws IOException, ClassNotFoundException {
		
		assert reader != null;
		return serializationPolicy.deserialize(reader.nextLine().getBytes(), archetype);
	}
	
	@Override
	public void writeObject(Object obj) throws IOException {
		
		assert writer != null;

		writer.println(new String(serializationPolicy.serialize(obj)));
	}
	
	public void sendSnapshot(Parcheggio.Snapshot snapshot) throws IOException {
		
		writeObject(new ParcheggioNetMessage(NetMessageType.SNAPSHOT_UPDATE, snapshot));
	}
	
}
