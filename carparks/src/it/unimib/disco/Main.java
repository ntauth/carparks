package it.unimib.disco;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {
	
		// Run one simulation where 1 minute = 2 seconds and approx. 3 cars/minute request to park,
		// 10 parking slots, 5 valets
		Executors.newFixedThreadPool(1).submit(new ParcheggioSimulator(3.0, 10, 5, TimeUnit.MINUTES, TimeUnit.SECONDS));
		
		// Wait for input
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}
	
}

