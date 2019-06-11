package it.unimib.disco.utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ChronoScaler {

	public static final long       DEFAULT_TICK_MULTIPLIER = 1l;
	public static final ChronoUnit DEFAULT_CHRONO_UNIT = ChronoUnit.SECONDS;
	
	private static final Map<Class<?>, ChronoScaler> _instances;
	
	private ChronoUnit fromUnit;
	private ChronoUnit toUnit;
	private long tickMultiplier;

	static {
		_instances = new HashMap<>();
	}
	
	private ChronoScaler(ChronoUnit fromUnit, ChronoUnit toUnit, long tickMultiplier) {
		
		this.tickMultiplier = tickMultiplier;
		this.fromUnit = fromUnit;
		this.toUnit = toUnit;
	}
	
	private ChronoScaler() {
		
		this(DEFAULT_CHRONO_UNIT, DEFAULT_CHRONO_UNIT, DEFAULT_TICK_MULTIPLIER);
	}
	
	public float getTickMultiplier() {
		
		return tickMultiplier;
	}
	
	public ChronoUnit getFromUnit() {
	
		return fromUnit;
	}
	
	public ChronoUnit getToUnit() {
		
		return toUnit;
	}
	
	public long scale(long fromAmount) {
		
		return Duration.of(fromAmount, fromUnit).multipliedBy(tickMultiplier).get(toUnit);
	}
	
	public static synchronized ChronoScaler getInstance(Class<?> cls,  ChronoUnit fromUnit, ChronoUnit toUnit, long tickMultiplier) {
		
		ChronoScaler chrono;
		
		if (!_instances.containsKey(cls)) {
			
			_instances.put(cls, (chrono = new ChronoScaler(fromUnit, toUnit, tickMultiplier)));
		}
		else
			chrono = _instances.get(cls);
		
		return chrono;
	}
	
	public static synchronized void destroyInstance(Class<?> cls) {
		
		if (!_instances.containsKey(cls)) {
			
			_instances.remove(cls);
		}
	}
	
}
