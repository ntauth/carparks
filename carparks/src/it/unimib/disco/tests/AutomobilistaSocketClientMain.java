package it.unimib.disco.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
	private String[] slots = {"00:00"};
	
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
					System.out.println("Book a parking");
					System.out.println();
					List<Snapshot> snapshots = client.getParcheggioSnapshots();
					for (int i = 0; i < snapshots.size(); i++)
						System.out.printf("[%d] %s\n", i, snapshots.get(i).getParcheggioName());
					System.out.println();
					int parking = getInput(String.format("Enter parking to book. (0-%d)\n", snapshots.size()-1),
										   String.format("Please enter value between %d and %d.\n", 0, snapshots.size()-1),
										   0, snapshots.size()-1);
					Object[] slots = getAvailableSlots(48);
					System.out.printf("Enter slot from %s to: \n", slots[0]);
					for (int i = 0; i < slots.length; i++)
						System.out.printf("[%d] %s\n", i+1, slots[i]);
					int slot = getInput("",
										String.format("Please enter values between %d and %d.\n", 1, slots.length+1),
										1, slots.length+1);
					System.out.printf("Sending request to %s, from %s to %s\n", 
										snapshots.get(parking).getParcheggioName(),
										slots[0],
										slots[slot-1]);
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
	
	public Object[] getAvailableSlots(int capSize)
	{
		ArrayList<String> s = new ArrayList<String>();
		Calendar rightNow = Calendar.getInstance();
		int hour = rightNow.get(Calendar.HOUR_OF_DAY);
		int slots = 0;
		for (int minutes = 0,hours = hour; 
			(hours < 24) && slots < capSize; 
			minutes+= 30)
		{
			slots++;
			if (minutes == 60)
			{
				hours++;
				minutes = 0;
			}
			s.add(String.format("%02d:%02d", hours, minutes));
		}
		return s.toArray();
	}
	
}


