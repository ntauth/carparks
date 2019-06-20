package it.unimib.disco;

import java.util.Scanner;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args) {
	
		// Run one simulation where 1 minute = 1 second and approx. 3 cars/minute (0.05 per second) request to park,
		// 10 parking slots, 5 valets
		Executors.newFixedThreadPool(1).submit(new ParcheggioSimulator(3, 10, 5));
		
		// Wait for input
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		
		System.exit(0);
	}
	
}

