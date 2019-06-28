package it.unimib.disco.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import it.unimib.disco.domain.Parcheggiatore;
import it.unimib.disco.domain.Parcheggio;

public class ParcheggioSocketClientMain implements Runnable {

	private static final int DEFAULT_NO_OF_PARCHEGGI = 5;
	private static final int DEFAULT_NO_OF_VALETS = 10;
	
	private static final String[] DEFAULT_PARCHEGGI_NAMES = {
			"Car Silos, Autostadt",
			"Rheinauhafen Parking Tunnel",
			"Herma Parking Building",
			"Parkhaus Engelenschanze",
			"Eureka Car Park",
	};
	
	private static final String DEFAULT_PLATFORM_IP = "127.0.0.1";
	private static final int DEFAULT_PLATFORM_PORT = 4242;
	
	private static final Logger _logger = Logger.getLogger(ParcheggioSocketClientMain.class.getName());
	
	private final ExecutorService executor;
	private final List<Parcheggio> parcheggi;
	
	private String platformIp;
	private int platformPort;
	
	public ParcheggioSocketClientMain(String ip, int port, int noOfParcheggi) {
		
		executor = Executors.newFixedThreadPool(noOfParcheggi);
		parcheggi = new ArrayList<>();
		
		platformIp = ip;
		platformPort = port;
		
		// Create parcheggi
		for (int i = 0; i < noOfParcheggi; i++) {
			
			List<Parcheggiatore> valets = new ArrayList<>();
			
			for (int j = 0; j < DEFAULT_NO_OF_VALETS; j++)
				valets.add(new Parcheggiatore());
			
			parcheggi.add(new Parcheggio(i, DEFAULT_PARCHEGGI_NAMES[i], 10, valets));
		}
	}
	
	public static void main(String[] args) {
		
		int noOfParcheggi = DEFAULT_NO_OF_PARCHEGGI;
		String ip;
		int port;
		
		if (args.length > 1) {
			
			ip = args[0];
			port = Integer.parseInt(args[1]);
		}
		else {
			
			ip = DEFAULT_PLATFORM_IP;
			port = DEFAULT_PLATFORM_PORT;
		}
		
		if(args.length > 2)
			noOfParcheggi = Integer.parseInt(args[2]);
		
		new ParcheggioSocketClientMain(ip, port, noOfParcheggi).run();
	}

	@Override
	public void run() {
		
		final CountDownLatch barrier = new CountDownLatch(parcheggi.size());
		
		for (Parcheggio pk : parcheggi) {
			
			executor.submit(() -> {
				
					try {
						
						pk.connectToPlatform(platformIp, platformPort);
						pk.call();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						barrier.countDown();
					}
				});
		}
		
		try {
			barrier.await();
		}
		catch (InterruptedException e) {
			_logger.severe(e.getMessage());
		}
	}

}
