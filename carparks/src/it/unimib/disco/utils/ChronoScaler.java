package it.unimib.disco.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChronoScaler {

	public static final long DEFAULT_TICK_MULTIPLIER = 1l;
	public static final TimeUnit DEFAULT_CHRONO_UNIT = TimeUnit.SECONDS;
	
	private static final Map<Class<?>, ChronoScaler> _instances;
	
	private TimeUnit fromUnit;
	private TimeUnit toUnit;
	private long tickMultiplier;

	static {
		_instances = new HashMap<>();
	}
	
	private ChronoScaler(TimeUnit fromUnit, TimeUnit toUnit, long tickMultiplier) {
		
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
	
	public TimeUnit getFromUnit() {
	
		return fromUnit;
	}
	
	public TimeUnit getToUnit() {
		
		return toUnit;
	}
	
	public long scale(long duration, Direction direction) {
		
		long amt;
		
		if (direction == Direction.TO)
			amt = fromUnit.convert(duration / tickMultiplier, toUnit);
		else
			amt = toUnit.convert(duration * tickMultiplier, fromUnit);
		
		return amt;
	}
	
	public long scale(long amount) {
		
		return scale(amount, Direction.TO);
	}
	
	public static synchronized ChronoScaler getInstance(Class<?> cls,  TimeUnit fromUnit, TimeUnit toUnit, long tickMultiplier) {
		
		ChronoScaler chrono;
		
		if (!_instances.containsKey(cls)) {
			
			_instances.put(cls, (chrono = new ChronoScaler(fromUnit, toUnit, tickMultiplier)));
		}
		else
			chrono = _instances.get(cls);
		
		return chrono;
	}
	
	public static synchronized ChronoScaler getInstance(Class<?> cls,  TimeUnit fromUnit, TimeUnit toUnit) {
		
		return getInstance(cls, fromUnit, toUnit, DEFAULT_TICK_MULTIPLIER);
	}
	
	public static synchronized void destroyInstance(Class<?> cls) {
		
		if (!_instances.containsKey(cls)) {
			
			_instances.remove(cls);
		}
	}
	
	public enum Direction {
		
		FROM,
		TO
	}
	
}
