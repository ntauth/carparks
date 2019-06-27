package it.unimib.disco.tests;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import it.unimib.disco.domain.Parcheggio.Snapshot;
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
			System.out.println("[0] Exit");
			System.out.println("[1] See available parking ");
			System.out.println("[2] Book a parking");
			String input = in.nextLine();
			
			
			if(input.equals("0"))
					break;
			if (input.equals("1"))
			{
				cls();
				try {
					List<Snapshot> snapshots = client.getParcheggioSnapshots();
					for (int i = 0; i < snapshots.size(); i++)
						System.out.printf("[%d] %s", i, snapshots.get(i).getParcheggioName());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (input.equals("2"))
			{
				cls();
				try {
					List<Snapshot> snapshots = client.getParcheggioSnapshots();
					for (int i = 0; i < snapshots.size(); i++)
						System.out.printf("[%d] %s\n", i, snapshots.get(i).getParcheggioName());
					System.out.printf("Enter parking to book. (0-%d)\n", snapshots.size());
					int parking = Integer.parseInt(in.nextLine());
					System.out.printf("Enter slot\n", snapshots.size());
					int slot = Integer.parseInt(in.nextLine());
					System.out.println("Sending request to " + parking + ", slot: " + slot);
					boolean result = client.reserveTimeSlot(snapshots.get(parking), slot);
					if(!result)
					{
						System.out.println("Something went wrong!");
					}
					else
					{
						System.out.println("Reservation successfull!");
					}
					System.out.println("Press any button to continue...");
					in.nextByte();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cls();
		}
		
		in.close();
	}
	
	private void cls()
	{
		System.out.printf("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
	}
}


