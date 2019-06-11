package it.unimib.disco.utils;

public class Chrono {
	
	public static long getCurrentTime(ChronoScaler scaler, ChronoScaler.Direction direction) {
		
		return scaler.scale(System.currentTimeMillis(), direction);
	}
	
	public static long getCurrentTime(ChronoScaler scaler) {
		
		return getCurrentTime(scaler, ChronoScaler.Direction.TO);
	}
	
	public static long getCurrentTime() {
		
		return System.currentTimeMillis();
	}
	
}
