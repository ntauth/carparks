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
					
					List<Snapshot> snapshots = new ArrayList<Snapshot>();
					for (Snapshot s : client.getParcheggioSnapshots())
						if (s.getFreeParkingSlots() > 0)
							snapshots.add(s);
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
					List<Snapshot> snapshots = new ArrayList<Snapshot>();
					for (Snapshot s : client.getParcheggioSnapshots())
						if (s.getFreeParkingSlots() > 0)
							snapshots.add(s);
					if (snapshots.size() != 0)
					{
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
						if(parking!=snapshots.size())
						{
							Object[] slots = getAvailableSlots();
							System.out.printf("Enter slot from %s to: \n", slots[slots.length-1]);
							for (int i = 0; i < slots.length; i++)
								System.out.printf("[%d] %s\n", i+1, slots[i]);
							
							int slot = getInput("",
											String.format("Please enter values between %d and %d.\n", 1, slots.length),
											1, slots.length);
							
							System.out.printf("Sending request to %s, from %s to %s\n", 
											snapshots.get(parking).getParcheggioName(),
											slots[slots.length-1],
											slots[slot-1]);
							
							Ticket ticket = client.reserveTimeSlot(snapshots.get(parking), slot);
							
							if(ticket==null)
								System.out.println("Time slots for this parking is not available!");
							else
								System.out.println("Reservation successfull! Ticket: " + ticket.getUuid());
							
						}
					}
					else
						System.out.println("No parking available!");
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
	
	public Object[] getAvailableSlots()
	{
		ArrayList<String> s = new ArrayList<String>();
		for (int minutes = 0,hours = 0; 
			hours < 24; 
			minutes+= 30)
		{
			if (minutes == 60)
			{
				hours++;
				minutes = 0;
			}
			s.add(String.format("%02d:%02d", hours, minutes));
		}
		s.remove(s.size()-1);
		Calendar rightNow = Calendar.getInstance();
		int total = rightNow.get(Calendar.HOUR_OF_DAY)*2;
		int minutes = rightNow.get(Calendar.MINUTE);
		if (minutes >= 30)
			total++;
		rotate(s, 47-total);
		return s.toArray();
	}
	
	private <T> ArrayList<T> rotate(ArrayList<T> aL, int shift)
	{
	    if (aL.size() == 0)
	        return aL;

	    T element = null;
	    for(int i = 0; i < shift; i++)
	    {
	        // remove last element, add it to front of the ArrayList
	        element = aL.remove( aL.size() - 1 );
	        aL.add(0, element);
	    }

	    return aL;
	}
	
}


