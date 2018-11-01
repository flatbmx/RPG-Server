package com.podts.rpg.server.model.universe;

import java.util.Iterator;
import java.util.Objects;

public class VectorIteratorShifter extends ShiftBehavior {
	
	public static final VectorIteratorShifter construct(Iterable<? extends Vector> it) {
		Objects.requireNonNull(it);
		return construct(it.iterator());
	}
	
	public static final VectorIteratorShifter construct(Iterator<? extends Vector> it) {
		Objects.requireNonNull(it);
		return new VectorIteratorShifter(it);
	}
	
	protected final Iterator<? extends Vector> it;
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}
	
	@Override
	public Vector next() {
		return it.next();
	}
	
	VectorIteratorShifter(Iterator<? extends Vector> it) {
		this.it = it;
	}
	
	VectorIteratorShifter(Iterable<? extends Vector> it) {
		this(it.iterator());
	}
	
}
