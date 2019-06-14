package it.unimib.disco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import it.unimib.disco.entities.Automobile;
import it.unimib.disco.entities.Automobilista;
import it.unimib.disco.entities.Parcheggiatore;
import it.unimib.disco.utils.Chrono;
import it.unimib.disco.utils.PoissonProcess;

/**
 * @todo Implement the distribution as a strategy
 * 
 */
public final class ParcheggioSimulator implements Callable<Void> {

	private final static Logger _logger;
	
	private final Parcheggio parcheggio;
	private final List<Parcheggiatore> parcheggiatori;
	private final ExecutorService executor;
	
	private final Random rng;
	private final PoissonProcess poisson;
	private final TimeUnit srcUnit;
	private final TimeUnit dstUnit;
	
	private long timeStep;
	
	static {
		_logger = Logger.getLogger(ParcheggioSimulator.class.getName());
	}
	
	/**
	 * 
	 * @param lambda Rate at which cars request to park
	 * @param freeParkingSlots Initial free parking slots
	 * @param freeParcheggiatori Initial free valets
	 * @param scaler Time scaler
	 */
	public ParcheggioSimulator(double lambda, int freeParkingSlots, int freeParcheggiatori, TimeUnit srcUnit, TimeUnit dstUnit) {
		
		this.parcheggiatori = new ArrayList<>();
		
		for (int i = 0; i < freeParcheggiatori; i++)
			parcheggiatori.add(new Parcheggiatore());
		
		this.parcheggio = new Parcheggio(freeParkingSlots, parcheggiatori);
		this.executor = Executors.newCachedThreadPool();
		
		this.rng = new Random(Chrono.getCurrentTime());
		this.poisson = new PoissonProcess(lambda, rng);
		this.srcUnit = srcUnit;
		this.dstUnit = dstUnit;
		
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
			
			_logger.info(String.format("[P] Time Step: %d", timeStep));
			
			double ppDelta = poisson.timeForNextEvent();
			long delta = (long) (10 * ppDelta);
			dstUnit.sleep(delta);
			
			Automobile auto = new Automobile();
			Automobilista owner = new Automobilista(auto, parcheggio, true);
			
			executor.submit(() -> {
				
				owner.consegna();
				
				/**
				 * @todo Replace with a normal random variate
				 */
				try { dstUnit.sleep(rng.nextLong() % 5); }
				catch (InterruptedException e) { }
				
				owner.preleva();
			});
			
			timeStep += delta;
		}
	}
	
}
