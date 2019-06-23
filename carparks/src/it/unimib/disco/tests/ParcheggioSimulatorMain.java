package it.unimib.disco.tests;

import java.util.Scanner;
import java.util.concurrent.Executors;

import it.unimib.disco.domain.ParcheggioSimulator;

public class ParcheggioSimulatorMain {

	private static final String DEFAULT_PLATFORM_IP = "127.0.0.1";
	private static final int DEFAULT_PLATFORM_PORT = 4242;
	
	public static void main(String[] args) {
	
		// Run one simulation where 1 minute = 1 second and approx. 3 cars/second request to park,
		// 10 parking slots, 5 valets
		ParcheggioSimulator ps = new ParcheggioSimulator(1, 10, 5);

		Executors.newFixedThreadPool(1).submit(ps);
		
		// Connect to platform
		ps.getParcheggio().connectToPlatform(DEFAULT_PLATFORM_IP, DEFAULT_PLATFORM_PORT);
		
		// Wait for input
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		
		System.exit(0);
	}
	
}

