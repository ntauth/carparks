package it.unimib.disco.tests;

import java.util.Scanner;
import java.util.concurrent.Executors;

import it.unimib.disco.config.NetConfig;
import it.unimib.disco.domain.ParcheggioSimulator;

/**
 * @brief Test class for ParcheggioSimulator
 *
 */
public class ParcheggioSimulatorMain {

	public static void main(String[] args) {
	
		double carsPerSecond = 2.0;
		int parkingSlots = 10;
		int valets = 5;
		
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
		
		if(args.length > 4)
		{
			carsPerSecond = Integer.parseInt(args[2]);
			parkingSlots = Integer.parseInt(args[3]);
			valets = Integer.parseInt(args[4]);
			
			if (valets <= 0 || carsPerSecond <= 0 || parkingSlots <= 0)
			{
				System.out.printf("Invalid values: %d, %f, %d. Every value must be > 0\n", 
								   valets,
								   carsPerSecond,
								   parkingSlots);
				return;
			}
		}
		
		// Run a simulation approx. 2 cars/second request to park, 10 parking slots, 5 valets
		ParcheggioSimulator ps = new ParcheggioSimulator(carsPerSecond, parkingSlots, valets);

		Executors.newFixedThreadPool(1).submit(ps);
		
		// Connect to platform
		ps.getParcheggio().connectToPlatform(NetConfig.DEFAULT_PLATFORM_IP, NetConfig.DEFAULT_PLATFORM_PORT);
		
		// Wait for input
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		
		System.exit(0);
	}
	
}

