package it.unimib.disco.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.function.Consumer;

public abstract class SocketClientBase extends Observable implements ISocketClient {

	protected ISerializationPolicy serializationPolicy;
	protected Socket socket;
	protected SocketClientConnectionStatus connectionStatus;
	protected InputStream istream;
	protected OutputStream ostream;
	
	public SocketClientBase(ISerializationPolicy serializationPolicy) {
		
		this.serializationPolicy = serializationPolicy;
	}
	
	public SocketClientBase() {
		
		this(new DefaultSerializationPolicy());
	}
	
	@Override
	public void connect(String ip, int port, Consumer<SocketClientConnectionEventArgs> connectionStatusChangedDelegate) {
		
		addObserver((self, args) -> {
			
			if (connectionStatusChangedDelegate != null)
				connectionStatusChangedDelegate.accept((SocketClientConnectionEventArgs) args);
		});
		
		try {
			
			// onConnecting
			onConnectStatusChanged(new SocketClientConnectionEventArgs(SocketClientConnectionStatus.CONNECTING));
			
			socket = new Socket(ip, port);
			istream = socket.getInputStream();
			ostream = socket.getOutputStream();
			
			// onConnected
			onConnectStatusChanged(new SocketClientConnectionEventArgs(SocketClientConnectionStatus.CONNECTED));
		}
		catch (IOException e) {
			
			// onError
			onConnectStatusChanged(new SocketClientConnectionEventArgs(SocketClientConnectionStatus.ERROR, e));
		}
	}

	protected void onConnectStatusChanged(SocketClientConnectionEventArgs args) {
		
		connectionStatus = args.getStatus();
		
		setChanged();
		notifyObservers(args);
	}
	
	public SocketClientConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public ISerializationPolicy getSerializationPolicy() {
		return serializationPolicy;
	}
	
}
