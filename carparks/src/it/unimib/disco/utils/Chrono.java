package it.unimib.disco.utils;

public class Chrono {

	public static long getCurrentTime(ChronoScaler scaler) {
		
		return scaler.scale(System.currentTimeMillis());
	}
	
}
