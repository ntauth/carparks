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
	
		// Run a simulation approx. 2 cars/second request to park, 10 parking slots, 5 valets
		ParcheggioSimulator ps = new ParcheggioSimulator(2.0, 10, 5);

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

