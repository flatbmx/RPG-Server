package com.podts.rpg.server.model.universe;

public abstract class VectorLocation extends Location {
	
	public static final Vector validate(Vector v) {
		if(v == null)
			return Vector.ZERO;
		return v;
	}
	
	private final Vector vector;
	
	@Override
	public Vector asVector() {
		return vector;
	}
	
	@Override
	public int getX() {
		return asVector().getX();
	}

	@Override
	public int getY() {
		return asVector().getY();
	}
	
	@Override
	public int getZ() {
		return asVector().getZ();
	}
	
	VectorLocation(Vector vector) {
		this.vector = validate(vector);
	}
	
	VectorLocation(int x, int y, int z) {
		vector = new Vector(x, y, z);
	}
	
	VectorLocation(int x, int y) {
		this(x, y, 0);
	}
	
}
