package it.unimib.disco.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import it.unimib.disco.utils.Chrono;
import it.unimib.disco.utils.PoissonProcess;

public final class ParcheggioSimulator implements Callable<Void> {

	private final static Logger _logger;
	
	private final Parcheggio parcheggio;
	private final List<Parcheggiatore> parcheggiatori;
	private final ExecutorService executor;
	
	private final Random rng;
	private final PoissonProcess poisson;
	private final double carParkRate;
	
	private long timeStep;
	
	static {
		_logger = Logger.getLogger(ParcheggioSimulator.class.getName());
	}
	
	/**
	 * 
	 * @param lambda Rate at which cars request to park
	 * @param freeParkingSlots Initial number of free parking slots
	 * @param freeParcheggiatori Initial number of free valets ("parcheggiatori")
	 */
	public ParcheggioSimulator(double lambda, int freeParkingSlots, int freeParcheggiatori) {
		
		this.parcheggiatori = new ArrayList<>();
		
		for (int i = 0; i < freeParcheggiatori; i++)
			parcheggiatori.add(new Parcheggiatore());
		
		this.parcheggio = new Parcheggio(freeParkingSlots, parcheggiatori);
		this.executor = Executors.newCachedThreadPool();
		
		this.rng = new Random(Chrono.getCurrentTime());
		this.poisson = new PoissonProcess(lambda, rng);
		this.carParkRate = lambda;
		
		this.timeStep = 0;
	}

	@Override
	public Void call() throws Exception {
		
		runSimulation();
		
		return null;
	}
	
	private void runSimulation() throws InterruptedException {
		
		// Start parcheggio
		executor.submit(parcheggio);
		
		// Start parcheggiatori
		parcheggiatori.forEach((p) -> executor.submit(p));
		
		_logger.info("[P] Simulation started");
		
		// Simulation loop
		while (true) {
			
			_logger.info(String.format("[P] Time Step: ~ %d s. Free slots: %d - Free valets: %d", timeStep, 
					parcheggio.getFreeParkingSlots(), parcheggio.getFreeValets()));
			
			double delta = poisson.timeForNextEvent();
			long deltaMs = (long) (1000 * delta);
			
			TimeUnit.MILLISECONDS.sleep(deltaMs);
			
			Automobile auto = new Automobile();
			Automobilista owner = new Automobilista(auto, parcheggio, true);
			
			executor.submit(() -> {
				
				owner.consegna();

				/**
				 * @todo Replace with a normal random variate
				 */
				try {
					
					owner.waitOnTicket();
					TimeUnit.MILLISECONDS.sleep((long) (1000 / carParkRate)); 
				}
				catch (InterruptedException e) { }
				
				owner.preleva();
			});
			
			timeStep += deltaMs / 1000;
		}
	}

	public Parcheggio getParcheggio() {
		return parcheggio;
	}
	
}
