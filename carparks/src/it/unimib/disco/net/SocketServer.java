package it.unimib.disco.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import it.unimib.disco.domain.Parcheggio.Snapshot;


public class SocketServer implements Callable<Void> {
	
	protected ISerializationPolicy policy;
	protected ExecutorService executor;
	protected ServerSocket serverSocket;
	protected List<Socket> clientSockets;
	protected Lock clientSocketsLock;
	protected AtomicBoolean abortListenLoop;
	protected Map<Integer, Snapshot> snapshots;
	
	public SocketServer(int port) throws IOException {
		
		policy = new JsonSerializationPolicy();
		executor = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		clientSockets = new ArrayList<>();
		clientSocketsLock = new ReentrantLock();
		abortListenLoop = new AtomicBoolean(false);
		snapshots = new HashMap<Integer, Snapshot>();
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
		Scanner in = null;
		String lineIn;
		
		try {
			
			out = new PrintWriter(client.getOutputStream());
			in = new Scanner(client.getInputStream());
			
			/* Ricevo un messaggio con dentro una richiesta,
			 * lo deserializzo, lo elaboro, ed alla fine
			 * invio la risposta.
			 */
			while ((lineIn = in.nextLine()) != null) {
				
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
				out.flush();
			}
		}
		catch (Exception e) {
			// Problema con il client, termina la connessione e basta.
			// Chiudere gli stream
			e.printStackTrace();
			return;
		}
		finally {
			
			try {
				
				out.close();
				in.close();
				client.close();
			}
			catch (IOException e) {
				e.printStackTrace();
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
		
		switch (messageType)
		{
			case AVAILABLE:
				List<Snapshot> freeParking = snapshots.values().stream()
												.filter(s -> s.getFreeParkingSlots() > 0)
												.collect(Collectors.toList());
				response = new ClientNetMessage(NetMessageType.AVAILABLE, 
												freeParking);
				break;
			case BOOK:
				// Book specified parking
			default:
				break;
		}
		return response;
	}
	
	private ParcheggioNetMessage processParcheggioMessage(ParcheggioNetMessage message)
	{
		ParcheggioNetMessage response = null;
		NetMessageType messageType = message.getType();
		
		switch (messageType)
		{
			case SNAPSHOT_UPDATE:
				Snapshot messageSnapshot = message.getParking();
				this.snapshots.put(messageSnapshot.getParcheggioId(), messageSnapshot);
				System.out.printf("Updated %s, to %d spots\n",
										messageSnapshot.getParcheggioId(),
										messageSnapshot.getFreeParkingSlots());
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
