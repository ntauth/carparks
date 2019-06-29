package it.unimib.disco.utils;

/**
 * @brief Synchronization Primitives
 *
 */
public class SynchroPrimitives {

	public static Boolean testAndSet(Boolean bool) {
		
		Boolean prev = bool;
		
		if (!bool)
			bool = true;
		
		return prev;
	}
	
}
