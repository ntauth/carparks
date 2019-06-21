package it.unimib.disco.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import it.unimib.disco.domain.Parcheggio.Snapshot;


public class SocketServer implements Callable<Void> {
	
	protected ISerializationPolicy policy;
	protected ExecutorService executor;
	protected ServerSocket serverSocket;
	protected List<Socket> clientSockets;
	protected Lock clientSocketsLock;
	protected AtomicBoolean abortListenLoop;
	protected List<Snapshot> snapshots;
	
	public SocketServer(int port) throws IOException {
		
		policy = new JsonSerializationPolicy();
		executor = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		clientSockets = new ArrayList<>();
		clientSocketsLock = new ReentrantLock();
		abortListenLoop = new AtomicBoolean(false);
		snapshots = new ArrayList<Snapshot>();
	}

	@Override
	public Void call() throws Exception {
		
		while (abortListenLoop.get() != true) {
			
			Socket clientSocket = serverSocket.accept();
			
			clientSocketsLock.lock();
			clientSockets.add(clientSocket);
			clientSocketsLock.unlock();
			
			executor.submit(() -> handleClient(clientSocket));
		}
		
		return null;
	}
	
	/**
	 * @brief Handles client/server communication (JSON over TCP)
	 * 
	 * @param client The client to handle
	 */
	protected void handleClient(Socket client) {
		
		//Per la lettura e scrittura client/server
		PrintWriter out = null;
		BufferedReader in = null;
		String lineIn;
		
		try {
			
			out = new PrintWriter(client.getOutputStream());
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			/* Ricevo un messaggio con dentro una richiesta,
			 * lo deserializzo, lo elaboro, ed alla fine
			 * invio la risposta.
			 */
			while ((lineIn = in.readLine()) != null) {
				NetMessage message = 
						(NetMessage) policy.deserialize(lineIn.getBytes(), 
														NetMessage.class);
				
				NetMessage response = null;
				if (message instanceof ClientNetMessage)
					response = processClientMessage((ClientNetMessage) message);
				else if (message instanceof ParcheggioNetMessage)
					response = processParcheggioMessage((ParcheggioNetMessage) message);
				
				String jsonString = new String(policy.serialize(response));
				out.println(jsonString);
			}
		}
		catch (Exception e) {
			// Problema con il client, termina la connessione e basta.
			// Chiudere gli stream
			return;
		}
		finally {
			
			try {
				
				out.close();
				in.close();
				client.close();
			}
			catch (IOException e) {
				// Eccezione da non gestire.
			}
			
			onClientClose(client);
		}	
	}
	/**
	 * @param request Client request.
	 * @return Response of the processed request.
	 */
	private ClientNetMessage processClientMessage(ClientNetMessage request)
	{
		ClientNetMessage response = null;
		NetMessageType messageType = request.getType();
		switch(messageType)
		{
			case AVAILABLE:
				List<Snapshot> availableParking = 
					new ArrayList<Snapshot>();
				for (Snapshot s : snapshots)
				{
					if(s.getFreeParkingSlots()>0)
						availableParking.add(s);
				}
				response = new ClientNetMessage(NetMessageType.AVAILABLE, 
												availableParking);
			break;
			case BOOK:
				//Book specified parking
			default:
				break;
		}
		return response;
	}
	
	private ParcheggioNetMessage processParcheggioMessage(ParcheggioNetMessage message)
	{
		ParcheggioNetMessage response = null;
		NetMessageType messageType = message.getType();
		switch(messageType)
		{
			case SNAPSHOT_UPDATE:
				Snapshot messageSnapshot = message.getParking();
				int toChange = 0;
				for (toChange = 0; toChange < this.snapshots.size(); toChange++)
				{
					Snapshot current = this.snapshots.get(toChange);
					if (current.getParcheggioId() == messageSnapshot.getParcheggioId())
						break;				
				}
				this.snapshots.remove(toChange);
				this.snapshots.add(messageSnapshot);
				break;
			default:
				break;
		}
		return response;
	}
	
	/**
	 * @brief Disposes of the client being closed
	 * 
	 * @param client The client being closed
	 */
	protected void onClientClose(Socket client) {
		
		clientSocketsLock.lock();
		
		int clientIndex = -1;
		
		for (int i = 0; i < clientSockets.size(); i++) {
			
			if (clientSockets.get(i) == client)
				clientIndex = i;
		}
		
		if (clientIndex != -1)
			clientSockets.remove(clientIndex);
		
		clientSocketsLock.unlock();
	}
	
}
