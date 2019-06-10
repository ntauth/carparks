package it.unimib.disco.utils;

public class SynchroPrimitives {

	/**
	 * @brief Test and Set
	 * 
	 */
	public static Boolean testAndSet(Boolean bool) {
		
		Boolean prev = bool;
		
		if (!bool)
			bool = true;
		
		return prev;
	}
}
