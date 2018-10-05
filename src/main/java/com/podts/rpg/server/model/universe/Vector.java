package com.podts.rpg.server.model.universe;

public class Vector {
	
	private final int x, y, z;
	
	public final int getX() {
		return x;
	}
	
	public final int getY() {
		return y;
	}
	
	public final int getZ() {
		return z;
	}
	
	public Vector add(Vector other) {
		return new Vector(getX() + other.getX()
				, getY() + other.getY()
				, getZ() + other.getZ());
	}
	
	public Vector subtract(Vector other) {
		return new Vector(getX() - other.getX()
				, getY() - other.getY()
				, getZ() - other.getZ());
	}
	
	public Vector scale(int scale) {
		return new Vector(getX() * scale
				, getY() * scale
				, getZ() * scale);
	}
	
	public Vector invert() {
		return scale(-1);
	}
	
	public final double getLength() {
		return Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2) + Math.pow(getZ(), 2));
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this == o) return true;
		if(o instanceof Vector) {
			Vector v = (Vector)o;
			return getX() == v.getX()
					&& getY() == v.getY()
					&& getZ() == v.getZ();
		}
		return false;
	}
	
	Vector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
}
