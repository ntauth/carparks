package it.unimib.disco.tests;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import it.unimib.disco.domain.Parcheggio.Snapshot;
import it.unimib.disco.domain.Ticket;
import it.unimib.disco.net.AutomobilistaSocketClient;
import it.unimib.disco.net.serialization.JsonSerializationPolicy;

public class AutomobilistaSocketClientMain implements Runnable{
	
	private static final String DEFAULT_PLATFORM_IP = "127.0.0.1";
	private static final int DEFAULT_PLATFORM_PORT = 4242;
	
	private String platformIp;
	private int platformPort;
	
	private AutomobilistaSocketClient client;
	
	public AutomobilistaSocketClientMain(String ip, int port)
	{
		platformIp = ip;
		platformPort = port;
		client = new AutomobilistaSocketClient(new JsonSerializationPolicy());
	}
	
	public static void main (String[] args)
	{
		String ip;
		int port;
		
		if (args.length > 1)
		{
			ip = args[0];
			port = Integer.parseInt(args[1]);
		}
		else
		{
			ip = DEFAULT_PLATFORM_IP;
			port = DEFAULT_PLATFORM_PORT;
		}
		
		new AutomobilistaSocketClientMain(ip, port).run();
		
	}

	@Override
	public void run() {
		
		Scanner in = new Scanner(System.in);
		System.out.printf("Attempting connection with %s:%d...\n", 
							platformIp, 
							platformPort);
		client.connect(platformIp, platformPort);
		System.out.printf("Done.\n");
		
		while (true)
		{
			int input = getInput("[0] Exit\n[1] See available parking\n[2] Book a parking\n",
								 String.format("Please enter values between %d and %d.\n", 0, 2),
								 0, 2);
			
			if(input == 0)
					break;
			if (input == 1)
			{
				cls();
				System.out.println("Currently available parking");
				System.out.println();
				try {
					List<Snapshot> snapshots = client.getParcheggioSnapshots();
					for (int i = 0; i < snapshots.size(); i++)
						System.out.printf("%s\n", snapshots.get(i).getParcheggioName());
					System.out.println();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (input == 2)
			{
				try {
					cls();
					System.out.println("Book a parking");
					System.out.println();
					List<Snapshot> snapshots = client.getParcheggioSnapshots();
					for (int i = 0; i < snapshots.size(); i++)
						System.out.printf("[%d] %s\n", i, snapshots.get(i).getParcheggioName());
					System.out.println();
					int parking = getInput(String.format("Enter parking to book. (0-%d)\n", snapshots.size()-1),
										   String.format("Please enter value between %d and %d.\n", 0, snapshots.size()-1),
										   0, snapshots.size()-1);
					System.out.printf("Enter slot\n", snapshots.size());
					int slot = getInput(String.format("Enter slot\n", snapshots.size()),
										String.format("Please enter values between %d and %d.\n", 1, 48),
										1, 48);
					System.out.println("Sending request to " + 
										snapshots.get(parking).getParcheggioName() + 
										", slot: " + 
										slot);
					Ticket ticket = client.reserveTimeSlot(snapshots.get(parking), slot);
					if(ticket==null)
						System.out.println("Something went wrong!");
					else
						System.out.println("Reservation successfull! Ticket: " + ticket.getUuid());
					System.out.println();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		in.close();
	}
	
	private int getInput(String messagePrompt, String messageFail, int minVal, int maxVal)
	{
		Scanner in = new Scanner(System.in);
		int result;
		while(true)
		{
			System.out.print(messagePrompt);
			try
			{
				result = Integer.parseInt(in.nextLine());
				if (result >= minVal && result <= maxVal)
					break;
				else
				{
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
	
	private void cls()
	{
	}
}


