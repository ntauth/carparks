package it.unimib.disco.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import it.unimib.disco.domain.Parcheggio.Snapshot;
import it.unimib.disco.net.message.ClientNetMessage;
import it.unimib.disco.net.message.NetMessage;
import it.unimib.disco.net.message.NetMessageType;
import it.unimib.disco.net.message.ParcheggioNetMessage;
import it.unimib.disco.net.serialization.ISerializationPolicy;
import it.unimib.disco.net.serialization.JsonSerializationPolicy;


public class SocketServer implements Callable<Void> {
	
	protected ISerializationPolicy policy;
	protected ExecutorService executor;
	protected ServerSocket serverSocket;
	protected List<Socket> clientSockets;
	protected Lock clientSocketsLock;
	protected AtomicBoolean abortListenLoop;
	protected Map<Integer, Snapshot> snapshots;
	protected Map<Integer, Socket> parcheggioSocketMap;
	protected Map<String, Socket> pendingRequests;
	
	public SocketServer(int port) throws IOException {
		
		policy = new JsonSerializationPolicy();
		executor = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		clientSockets = new ArrayList<>();
		clientSocketsLock = new ReentrantLock();
		abortListenLoop = new AtomicBoolean(false);
		snapshots = new HashMap<Integer, Snapshot>();
		parcheggioSocketMap = new HashMap<Integer, Socket>();
		pendingRequests = new HashMap<String, Socket>();
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
				if (message instanceof ClientNetMessage)
					processClientMessage((ClientNetMessage) message, out, client);
				else if (message instanceof ParcheggioNetMessage)
					processParcheggioMessage((ParcheggioNetMessage) message, client);
			}
		}
		catch (Exception e) {
			// Problema con il client, termina la connessione e basta.
			// Chiudere gli stream
			if (!(e instanceof NoSuchElementException))
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
	private void processClientMessage(ClientNetMessage request, PrintWriter out, Socket client) throws Exception
	{
		ClientNetMessage response = null;
		NetMessageType messageType = request.getType();
		
		switch (messageType)
		{
			case GET_AVAILABLE_SNAPSHOTS:
				List<Snapshot> freeParking = snapshots.values().stream()
												.filter(s -> s.getFreeParkingSlots() > 0)
												.collect(Collectors.toList());
				response = new ClientNetMessage(NetMessageType.GET_AVAILABLE_SNAPSHOTS, 
												freeParking);
				break;
			case RESERVE_TIME_SLOT:
				reserveTimeSlot(request.getSelectedSnapshot(), request.getSlot(), client);
			default:
				break;
		}
		if(response != null)
		{
			String jsonString = new String(policy.serialize(response));
			out.println(jsonString);
			out.flush();
		}
	}
	
	private void processParcheggioMessage(ParcheggioNetMessage message, Socket client) throws Exception
	{
		ClientNetMessage response = null;
		NetMessageType messageType = message.getType();
		PrintWriter out = null;
		
		switch (messageType)
		{
			case SNAPSHOT_UPDATE:
				System.out.println("Received a snapshot from " + message.getParking().getParcheggioName());
				Snapshot messageSnapshot = message.getParking();
				this.snapshots.put(messageSnapshot.getParcheggioId(), messageSnapshot);
				this.parcheggioSocketMap.put(messageSnapshot.getParcheggioId(), client);
				out = new PrintWriter(client.getOutputStream());
				out.println(new String(this.policy.serialize(ParcheggioNetMessage.EMPTY)));
				out.flush();
				break;
			case RESERVE_TIME_SLOT:
				Socket requestingClient = this.pendingRequests.get(message.getTracer());
				out = new PrintWriter(requestingClient.getOutputStream());
				response = new ClientNetMessage(message.getType(), message.getTicket(), message.getSlot());
				out.println(new String(this.policy.serialize(response)));
				out.flush();
				break;
			default:
				break;
		}
	}
	
	private void reserveTimeSlot(Snapshot toReserve, int timeSlot, Socket client) throws Exception
	{
		ParcheggioNetMessage booking = new ParcheggioNetMessage(NetMessageType.RESERVE_TIME_SLOT, 
																toReserve,
																timeSlot);
		Socket toBookParking = parcheggioSocketMap.get(toReserve.getParcheggioId());
		PrintWriter pw = new PrintWriter(toBookParking.getOutputStream(), true);
		String toSend =  new String(this.policy.serialize(booking));
		pw.println(toSend);
		this.pendingRequests.put(booking.getTracer(), client);
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
