package it.unimib.disco.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import it.unimib.disco.domain.Parcheggio;
import it.unimib.disco.net.message.ClientNetMessage;
import it.unimib.disco.net.message.NetMessageType;
import it.unimib.disco.net.serialization.ISerializationPolicy;

public class AutomobilistaSocketClient extends SocketClientBase {

	protected Scanner reader;
	protected PrintWriter writer;
	
	public AutomobilistaSocketClient() {
		
		super();
	}
	
	public AutomobilistaSocketClient(ISerializationPolicy serializationPolicy) {
		
		super(serializationPolicy);
	}
	
	@Override
	public void connect(String ip, int port, Consumer<SocketClientConnectionEventArgs> connectionStatusChangedDelegate) {
		
		super.connect(ip, port, connectionStatusChangedDelegate);
		
		if (connectionStatus == SocketClientConnectionStatus.CONNECTED) {
			
			reader = new Scanner(istream);
			writer = new PrintWriter(ostream, true);
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
	
	public List<Parcheggio.Snapshot> getParcheggioSnapshots() throws IOException, ClassNotFoundException {
		
		writeObject(new ClientNetMessage(NetMessageType.GET_AVAILABLE_SNAPSHOTS));
		ClientNetMessage response = (ClientNetMessage) readObject(ClientNetMessage.class);
		
		return response.getSnapshots();
	}
	
	public boolean reserveTimeSlot(Parcheggio.Snapshot snapshot, int timeSlot) throws IOException, ClassNotFoundException {
		
		boolean success;
		writeObject(new ClientNetMessage(NetMessageType.RESERVE_TIME_SLOT,
											snapshot,
											timeSlot));
		
		ClientNetMessage response = (ClientNetMessage) readObject(ClientNetMessage.class);
		
		success = response.getSlot() == timeSlot ? true : false;
		
		return success;
	}
	
}
