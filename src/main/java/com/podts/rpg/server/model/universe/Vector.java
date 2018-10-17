package com.podts.rpg.server.model.universe;

import java.util.stream.Stream;

public class Vector {

	public static final Vector construct(int x, int y, int z) {
		return new Vector(x, y, z);
	}
	
	public static final Vector construct(int x, int y) {
		return new Vector(x, y);
	}
	
	public static final Vector ZERO = new Vector(0, 0, 0) {
		@Override public Vector add(Vector v) {return v;};
		@Override public Vector scale(int scale) {return this;};
		@Override public Vector invert() {return this;};
		@Override public double getLength() {return 0d;};
		@Override public boolean isZero() {return true;};
		@Override public boolean isOpposite(Vector v) {return v.isZero();};
		@Override public Stream<? extends Vector> trace() {return Stream.of(this);};
		@Override public Stream<? extends Vector> traceEvery(int scale) {return trace();};
	};
	public static final Vector X_AXIS = new Vector(1, 0, 0) {
		@Override public Vector scale(int scale) {return new Vector(scale,0,0);};
	};
	public static final Vector Y_AXIS = new Vector(0, 1, 0) {
		@Override public Vector scale(int scale) {return new Vector(0,scale,0);};
	};
	public static final Vector Z_AXIS = new Vector(0, 0, 1) {
		@Override public Vector scale(int scale) {return new Vector(0,0,scale);};
	};

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

	public double getLength() {
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

	public boolean isZero() {
		return getX() == 0 &&
				getY() == 0 &&
				getZ() == 0;
	}
	
	public boolean isOpposite(Vector v) {
		return getX() == -1 * v.getX() &&
				getY() == -1 * v.getY() &&
				getZ() == -1 * v.getZ();
	}
	
	public Stream<? extends Vector> trace() {
		return Stream.iterate(ZERO, v -> v.add(this));
	}
	
	public Stream<? extends Vector> traceEvery(int scale) {
		if(scale == 0)
			return Stream.of(ZERO);
		final Vector scaled = scale(scale);
		return Stream.iterate(ZERO, v -> v.add(scaled));
	}
	
	Vector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	Vector(int x, int y) {
		this(x, y, 0);
	}

}
