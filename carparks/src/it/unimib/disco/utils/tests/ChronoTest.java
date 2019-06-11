package it.unimib.disco.utils.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import it.unimib.disco.utils.Chrono;
import it.unimib.disco.utils.ChronoScaler;

public class ChronoTest {

	private final ChronoScaler scaler = ChronoScaler.getInstance(ChronoTest.class, TimeUnit.SECONDS, TimeUnit.MINUTES, 500);
	
	@Test
	public void testScale() {
		
	}

}
