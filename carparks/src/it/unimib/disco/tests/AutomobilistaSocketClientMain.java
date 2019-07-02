package it.unimib.disco.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import it.unimib.disco.config.NetConfig;
import it.unimib.disco.domain.Parcheggio.Snapshot;
import it.unimib.disco.domain.Ticket;
import it.unimib.disco.net.AutomobilistaSocketClient;
import it.unimib.disco.net.SocketClientConnectionEventArgs;
import it.unimib.disco.net.SocketClientConnectionStatus;
import it.unimib.disco.net.serialization.JsonSerializationPolicy;

/**
 * Text-based Interface for @see Automobilista instances to interact
 * with multiple @see Parcheggio instances through @see SocketServer
 *
 */
public class AutomobilistaSocketClientMain implements Runnable {
	
	private static final Logger _logger;
	
	private String platformIp;
	private int platformPort;
	
	private Scanner scanner;
	
	private AutomobilistaSocketClient client;
	
	static {
		_logger = Logger.getLogger(AutomobilistaSocketClientMain.class.getName());
	}
	
	public AutomobilistaSocketClientMain(String ip, int port) {
		
		platformIp = ip;
		platformPort = port;
		scanner = new Scanner(System.in);
		client = new AutomobilistaSocketClient(new JsonSerializationPolicy());
	}
	
	public static void main(String[] args) {
		
		String ip;
		int port;
		
		if (args.length > 1) {
			
			ip = args[0];
			port = Integer.parseInt(args[1]);
		}
		else {
			
			ip = NetConfig.DEFAULT_PLATFORM_IP;
			port = NetConfig.DEFAULT_PLATFORM_PORT;
		}
		
		new AutomobilistaSocketClientMain(ip, port).run();
	}
	
	private void onClientSocketStatusChange(AutomobilistaSocketClient sender, SocketClientConnectionEventArgs args) {
		
		switch (args.getStatus()) {
		
			case CONNECTED:
				System.out.println("Connected.");
				break;
				
			case CONNECTING:
				System.out.printf("Attempting connection with %s:%d...\n", 
						platformIp, 
						platformPort);
				break;
			
			case ERROR:
				System.out.printf("Connection to %s:%d failed.\n", 
						platformIp, 
						platformPort);
				break;
				
			default:
				break;
		}
	}
	
	@Override
	public void run() {
		
		Scanner in = new Scanner(System.in);

		client.addObserver((sender, args) -> onClientSocketStatusChange((AutomobilistaSocketClient) sender,
																		(SocketClientConnectionEventArgs) args));
		client.connect(platformIp, platformPort);
		
		while (client.getConnectionStatus() == SocketClientConnectionStatus.READY)
		{
			int input = getInput("[0] Exit\n[1] See available parking\n[2] Book a parking\n",
								 String.format("Please enter values between %d and %d.\n", 0, 2),
								 0, 2);
			
			if(input == 0) {
				break;
			}
			else if (input == 1) {
				
				System.out.println("Currently available parking");
				System.out.println();
				
				try {
					
					List<Snapshot> snapshots = new ArrayList<Snapshot>();
					List<Snapshot> receivedSnapshots = client.getParcheggioSnapshots();
					
					for (Snapshot s : receivedSnapshots)
						if (s.getFreeParkingSlots() > 0)
							snapshots.add(s);
					
					for (int i = 0; i < snapshots.size(); i++)
						System.out.printf("%s\n", snapshots.get(i).getParcheggioName());
					System.out.println();
					
				}
				catch (ClassNotFoundException e) {		
					_logger.severe(e.getLocalizedMessage());
				}
				catch (IOException e) {
					_logger.severe(e.getLocalizedMessage());
				}
			}
			else if (input == 2) {
				
				try {
					
					System.out.println("Book a parking");
					System.out.println();
					
					List<Snapshot> snapshots = new ArrayList<Snapshot>();
					List<Snapshot> receivedSnapshots = client.getParcheggioSnapshots();
					
					for (Snapshot s : receivedSnapshots)
						if (s.getFreeParkingSlots() > 0)
							snapshots.add(s);
					
					if (snapshots.size() != 0) {
						
						for (int i = 0; i < snapshots.size(); i++)
							System.out.printf("[%d] %s\n", i, snapshots.get(i).getParcheggioName());
						System.out.println();
						
						int parking = getInput(String.format("Enter parking to book. (0-%d) (%d to abort)\n", 
															snapshots.size()-1, 
															snapshots.size()),
											   String.format("Please enter value between %d and %d.\n", 0, 
													   		 snapshots.size()),
											   0, 
											   snapshots.size());
						
						if (parking != snapshots.size()) {
							
							Object[] slots = getAvailableSlots();
							
							//Prints the available starting time
							for (int i = 0; i < slots.length; i++)
								System.out.printf("[%d] %s\n", i+1, slots[i]);
							
							int timeSlotStart = getInput("Select the starting time.\n",
											String.format("Please enter values between %d and %d.\n", 1, slots.length),
											1, slots.length);
							
							//Prints the available finishing time
							for (int i = timeSlotStart; i < slots.length; i++)
								System.out.printf("[%d] %s\n", i+1, slots[i]);
							
							//The min value is 3 because there must be a minimum timeslot difference
							//between the start and the end.
							int timeSlotEnd = getInput("Select the ending time.\n",
									String.format("Please enter values between %d and %d.\n", timeSlotStart+1, slots.length),
									timeSlotStart+1, slots.length);
							
							System.out.printf("Sending request to %s, from %s to %s\n", 
											snapshots.get(parking).getParcheggioName(),
											slots[timeSlotStart-1],
											slots[timeSlotEnd-1]);
							
							Ticket ticket = client.reserveTimeSlot(snapshots.get(parking), timeSlotStart, timeSlotEnd);
							
							if (ticket == null)
								System.out.printf("Reservation from %s to %s is not available anymore!\n",
												  //slots[timeSlotStart],
												  //slots[timeSlotEnd]);
												  timeSlotStart,
												  timeSlotEnd);
							else
								System.out.printf("Reservation successfull! Ticket: %s\n",
													ticket.getUuid());
						}
					}
					else {
						System.out.println("No parking available!");
					}
					
					System.out.println();
				}
				catch (ClassNotFoundException e) {
					_logger.severe(e.getLocalizedMessage());
				}
				catch (IOException e) {
					_logger.severe(e.getLocalizedMessage());
				}
			}
		}
		
		in.close();
	}
	
	private int getInput(String messagePrompt, String messageFail, int minVal, int maxVal) {
		
		int result;
		
		while (true) {
			
			System.out.print(messagePrompt);
			
			try {
				
				result = Integer.parseInt(scanner.nextLine());
				
				if (result >= minVal && result <= maxVal) {
					break;
				}
				else {
					System.out.print(messageFail);
				}
			}
			catch (Exception e)
			{
				System.out.print(messageFail);
			}
		}
		
		return result;
	}
	
	public Object[] getAvailableSlots() {
		
		ArrayList<String> s = new ArrayList<String>();
		
		for (int minutes = 0, hours = 0; hours < 24; minutes += 30) {
			
			if (minutes == 60) {
				hours++;
				minutes = 0;
			}
			s.add(String.format("%02d:%02d", hours, minutes));
		}
		
		s.remove(s.size()-1);
		
		//Calendar rightNow = Calendar.getInstance();
		//int total = rightNow.get(Calendar.HOUR_OF_DAY)*2;
		//int minutes = rightNow.get(Calendar.MINUTE);
		
		//if (minutes >= 30)
		//	total++;
		
		//rotate(s, 47 - total);
		
		return s.toArray();
	}
	
}
