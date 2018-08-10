package com.podts.rpg.server;

import java.util.Spliterator;
import java.util.function.Consumer;

public final class Utils {
	
	private static final class TwoDArraySpliterator<T> implements Spliterator<T> {
		
		private final T[][] arr;
		
		private int x, y;
		private int sX, sY;
		private int eX, eY;
		
		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if(y > eY) return false;
			action.accept(arr[x++][y]);
			if(x > eX) {
				++y;
				x = sX;
			}
			return true;
		}

		@Override
		public Spliterator<T> trySplit() {
			int rowsLeft = eY - sY;
			if(rowsLeft > 0) {
				
			}
			return null;
		}

		@Override
		public long estimateSize() {
			long bulk = (eX - sX) * (eY - sY);
			if(x != sX)
				bulk -= eX - x;
			else if(x == sX)
				bulk += eX - sX;
			return bulk;
		}

		@Override
		public int characteristics() {
			return Spliterator.SIZED & Spliterator.ORDERED;
		}
		
		private TwoDArraySpliterator(T[][] arr, int sX, int sY, int eX, int eY) {
			this.arr = arr;
			this.sX = sX;
			this.sY = sY;
			this.eX = eX;
			this.eY = eY;
		}
		
		private TwoDArraySpliterator(T[][] arr) {
			this(arr, 0, 0, arr.length, arr[0].length);
		}
		
	}
	
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
