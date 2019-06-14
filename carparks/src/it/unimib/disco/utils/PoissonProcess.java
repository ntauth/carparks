package it.unimib.disco.utils;

import java.util.Random;

/**
 * Poisson process random number generation
 * 
 * Adapted from @see https://github.com/edrdo/PoissonProcess
 *
 */
public final class PoissonProcess {

	private final double lambda;
	private final Random rng;

	public PoissonProcess(double lambda, Random rng) {

		if (lambda <= 0d)
			throw new IllegalArgumentException("Supplied rate parameter is not positive: " + lambda);

		if (rng == null)
			throw new IllegalArgumentException("Null RNG argument");

		this.lambda = lambda;
		this.rng = rng;
	}

	public double getLambda() {
		return lambda;
	}

	public Random getRNG() {
		return rng;
	}

	public double timeForNextEvent() {
		// The sequence of inter-arrival times are independent and have an exponential distribution with mean 1/lambda.
		// To generate it we use the recipe in https://en.wikipedia.org/wiki/Exponential_distribution#Generating_exponential_variates
		return - Math.log(1.0 - rng.nextDouble()) / lambda;
	}

	/**
	 * Get number of events in an unit of time.
	 * @return Number of events.
	 */
	public int events() {
		return events(1d);
	}

	/**
	 * Get number of occurrences in time t (assumed to be relative to the unit time).
	 * @param time Length of time interval.
	 */
	public int events(double time) {
		// The algorithm based on inverse transform sampling is used -- @see:
		// https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
		int n = 0;
		double p = Math.exp(-lambda * time);
		double s = p;
		double u = rng.nextDouble();
		while (u > s) {
			n = n + 1;
			p = p * lambda / n;
			s = s + p;
		}    
		return n;
	}

}
