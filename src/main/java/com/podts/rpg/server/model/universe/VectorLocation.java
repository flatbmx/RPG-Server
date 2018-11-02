package com.podts.rpg.server.model.universe;

/**
 * A {@link Location} who's coordinates are internally represented as a {@link Vector}.
 * This class only needs to be extended to implement querying a {@link Plane}.
 * @author David
 *
 */
public abstract class VectorLocation extends Location {
	
	public static final Vector validate(Vector v) {
		if(v == null)
			return Vector.ZERO;
		return v;
	}
	
	private final Vector vector;
	
	@Override
	public final Vector asVector() {
		return vector;
	}
	
	@Override
	public final int getX() {
		return asVector().getX();
	}

	@Override
	public final int getY() {
		return asVector().getY();
	}
	
	@Override
	public final int getZ() {
		return asVector().getZ();
	}
	
	VectorLocation(Vector vector) {
		this.vector = vector;
	}
	
	VectorLocation(int x, int y, int z) {
		vector = new Vector(x, y, z);
	}
	
	VectorLocation(int x, int y) {
		this(x, y, 0);
	}
	
}
