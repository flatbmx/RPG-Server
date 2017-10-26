package com.podts.rpg.server;

public class Utils {
	
	public static final void assertNull(Object o, String message) {
		if(o == null) throw new NullPointerException(message);
	}
	
	public static final void assertNullArg(Object o, String message) {
		assertArg(o == null, message);
	}
	
	/**
	 * Throws a new IllegalArgumentException if the given condition is true.
	 * @param bool - The condition.
	 * @param message - The exception message.
	 */
	public static final void assertArg(boolean bool, String message) {
		if(bool) throw new IllegalArgumentException(message);
	}
	
	
	private Utils() {}
	
}
