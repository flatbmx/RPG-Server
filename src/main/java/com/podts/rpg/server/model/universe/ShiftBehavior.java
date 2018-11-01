package com.podts.rpg.server.model.universe;

import java.util.Iterator;

import com.podts.rpg.server.model.universe.Location.RelationalDirection;

public abstract class ShiftBehavior implements Iterator<Vector> {
	
	public static final ShiftBehavior EMPTY = new ShiftBehavior() {
		public boolean hasNext() {return false;}
		public Vector next() {return Vector.ZERO;}
	};
	
	public static final ShiftBehavior straight(final Vector vector) {
		return VectorShifter.construct(vector);
	}
	
	public static final ShiftBehavior spiral() {
		return spiralRight();
	}
	
	public static final ShiftBehavior spiralLeft() {
		return new SpiralShifter(RelationalDirection.LEFT);
	}
	
	public static final ShiftBehavior spiralRight() {
		return new SpiralShifter(RelationalDirection.RIGHT);
	}
	
}
