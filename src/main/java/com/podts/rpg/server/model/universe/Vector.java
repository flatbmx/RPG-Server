package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Represents an arrow or 2d-3d box.
 * To instantiate this class please call {@link #construct(int, int, int)} or {@link #construct(int, int)}.
 * This class should be final however due to overridden methods for {@link #ZERO} and the three basis vectors,
 * therefore this is non-final however the constructors have default(package) scope.
 * @author David
 *
 */
public class Vector {
	
	/**
	 * Returns a vector of (x,y,z).
	 * If you wish to have a zero vector please use {@link #ZERO} instead.
	 * @param x - The vectors x value
	 * @param y - The vectors y value
	 * @param z - The vectors z value
	 * @return Vector of (x,y,z).
	 */
	public static final Vector construct(int x, int y, int z) {
		return new Vector(x, y, z);
	}
	
	/**
	 * Returns a vector of (x,y,0).
	 * If you wish to have a zero vector please use {@link #ZERO} instead.
	 * @param x - The vectors x value
	 * @param y - The vectors y value
	 * @return Vector of (x,y,0).
	 */
	public static final Vector construct(int x, int y) {
		return new Vector(x, y);
	}
	
	public static final <V extends Vector> Vector add(Collection<V> vectors) {
		if(vectors == null || vectors.isEmpty())
			return ZERO;
		int x = 0,y = 0,z = 0;
		for(Vector v : vectors) {
			x += v.getX();
			y += v.getY();
			z += v.getZ();
		}
		return new Vector(x,y,z);
	}
	
	/**
	 * The identity element with respect to addition. Represented as (0,0,0).
	 */
	public static final Vector ZERO = new Vector(0, 0, 0) {
		@Override public Vector add(Vector v) {return v;};
		@Override public Vector scale(int scale) {return this;};
		@Override public Vector inverse() {return this;};
		@Override public double getLength() {return 0d;};
		@Override public boolean isZero() {return true;};
		@Override public boolean isOpposite(Vector v) {return false;};
		@Override public Stream<? extends Vector> trace() {return Stream.of(this);};
		@Override public Stream<? extends Vector> traceEvery(int scale) {return trace();};
	};
	public static final Vector X_AXIS = new Vector(1, 0, 0) {
		@Override public Vector scale(int scale) {return new Vector(scale,0,0);};
		@Override public double getLength() {return 1d;};
	};
	public static final Vector Y_AXIS = new Vector(0, 1, 0) {
		@Override public Vector scale(int scale) {return new Vector(0,scale,0);};
		@Override public double getLength() {return 1d;};
	};
	public static final Vector Z_AXIS = new Vector(0, 0, 1) {
		@Override public Vector scale(int scale) {return new Vector(0,0,scale);};
		@Override public double getLength() {return 1d;};
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
	
	public Vector add(Vector... vectors) {
		if(vectors.length == 0)
			return this;
		int nx = getX(), ny = getY(), nz = getZ();
		for(Vector v : vectors) {
			nx += v.getX();
			ny += v.getY();
			nz += v.getZ();
		}
		return new Vector(nx, ny, nz);
	}
	
	public static Vector add(Vector a, Vector b) {
		return a.add(b);
	}
	
	public static Vector subtract(Vector a, Vector b) {
		return a.subtract(b);
	}
	
	public Vector add(Vector other) {
		return new Vector(getX() + other.getX()
		, getY() + other.getY()
		, getZ() + other.getZ());
	}
	
	public Vector subtract(Vector... vectors) {
		if(vectors.length == 0)
			return this;
		int nx = getX(), ny = getY(), nz = getZ();
		for(Vector v : vectors) {
			nx -= v.getX();
			ny -= v.getY();
			nz -= v.getZ();
		}
		return new Vector(nx, ny, nz);
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
	
	public Vector inverse() {
		return scale(-1);
	}
	
	public double getLength() {
		return Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2) + Math.pow(getZ(), 2));
	}
	
	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + getX();
		hash = hash * 31 + getY();
		hash = hash * 31 + getZ();
		return hash;
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
	
	@Override
	public String toString() {
		return "(" + getX() + "," + getY() + "," + getZ() + ")";
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
