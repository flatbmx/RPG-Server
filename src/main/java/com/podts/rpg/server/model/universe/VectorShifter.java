package com.podts.rpg.server.model.universe;

import java.util.Objects;

public class VectorShifter extends ShiftBehavior {
	
	public static final VectorShifter construct(Vector vector) {
		Objects.requireNonNull(vector);
		return new VectorShifter(vector);
	}
	
	protected final Vector vector;
	
	public final Vector getVector() {
		return vector;
	}
	
	@Override
	public boolean hasNext() {
		return true;
	}
	
	@Override
	public Vector next() {
		return getVector();
	}
	
	VectorShifter(Vector vector) {
		this.vector = vector;
	}
	
}
