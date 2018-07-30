package com.podts.rpg.server.model.universe;

import java.util.Comparator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class Location implements Locatable, Cloneable {
	
	public enum Direction {
		UP(0,-1),
		LEFT(-1,0),
		DOWN(0,1),
		RIGHT(1,0);
		
		private static final Direction[] vals = Direction.values();
		
		public static final Stream<Direction> stream() {
			return Stream.of(vals);
		}
		
		public static final Direction get(Locatable first, Locatable second) {
			return get(first.getLocation(), second.getLocation());
		}
		
		public static final Direction get(Location first, Location second) {
			int dx = Integer.signum(second.getX() - first.getX());
			int dy = Integer.signum(second.getY() - first.getY());
			if((dx != 0 && dy != 0) ||
					(dx == 0 && dy == 0)) return null;
			for(Direction dir : vals) {
				if(dir.getX() == dx && dir.getY() == dy) return dir;
			}
			return null;
		}
		
		public static final Direction get(int dx, int dy) {
			dx = Integer.signum(dx);
			dy = Integer.signum(dy);
			for(Direction d : vals) {
				if(d.getX() == dx && d.getY() == dy)
					return d;
			}
			return null;
		}
		
		private final int dx, dy;
		
		public int getX() {
			return dx;
		}
		
		public int getY() {
			return dy;
		}
		
		public final Direction convert(RelationalDirection d) {
			return d.convert(this);
		}
		
		public final Location MoveFromLocation(Location origin, int distance) {
			return origin.shift(getX() * distance, getY() * distance);
		}
		
		public final Location MoveFromLocation(Location origin) {
			return origin.shift(getX(), getY());
		}
		
		private Direction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
		
	}
	
	public enum RelationalDirection {
		
		FORWARD(d -> d),
		BACKWARD(d -> Direction.vals[(d.ordinal() + 2) % Direction.vals.length] ),
		LEFT(d -> Direction.vals[(d.ordinal() + 1) % Direction.vals.length] ),
		RIGHT(d -> Direction.vals[Math.floorMod(d.ordinal() - 1, Direction.vals.length)] );
		
		private static final RelationalDirection[] vals = RelationalDirection.values();
		
		public static final Stream<RelationalDirection> stream() {
			return Stream.of(vals);
		}
		
		private UnaryOperator<Direction> operator;
		
		public Direction convert(Direction d) {
			return operator.apply(d);
		}
		
		private RelationalDirection(UnaryOperator<Direction> operator) {
			this.operator = operator;
		}
		
	}
	
	public enum MoveType {
		CREATE(),
		UPDATE(),
		DESTROY();
	}
	
	private class DistanceComparator implements Comparator<Locatable> {
		
		@Override
		public int compare(Locatable a, Locatable b) {
			return (int) (Location.this.distance(a) - Location.this.distance(b));
		}
		
	}
	
	private class WalkingDistanceComparator implements Comparator<Locatable> {
		
		@Override
		public int compare(Locatable a, Locatable b) {
			return Location.this.walkingDistance(a) - Location.this.walkingDistance(b);
		}
		
	}
	
	@Override
	public abstract Plane getPlane();
	
	public abstract int getX();
	public abstract int getY();
	public abstract int getZ();
	
	public Location shift(final int dx, final int dy, final int dz) {
		return getSpace().createLocation(getX() + dx, getY() + dy, getZ() + dz);
	}
	
	public Location shift(final int dx, final int dy) {
		return shift(dx, dy, 0);
	}
	
	@Override
	public final double distance(final Locatable other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate distance between a null Location.");
		if(!getSpace().equals(other.getSpace())) throw new IllegalArgumentException("Cannot calculate distance between points in different worlds.");
		final Location otherPoint = other.getLocation();
		if(getZ() != otherPoint.getZ()) throw new IllegalArgumentException("Cannot calculate distance between points in different Z planes.");
		return distance(otherPoint);
	}
	
	public final int WalkingDistance(final Locatable other) {
		if(other == null) throw new IllegalArgumentException("Cannot calculate walking distance between a null Location.");
		if(!getSpace().equals(other.getSpace())) throw new IllegalArgumentException("Cannot calculate walking distance between points in different worlds.");
		final Location otherPoint = other.getLocation();
		if(getZ() != otherPoint.getZ()) throw new IllegalArgumentException("Cannot calculate walking distance between points in different Z planes.");
		return walkingDistance(otherPoint);
	}
	
	final double distance(final Location otherPoint) {
		return Math.sqrt(Math.pow(getX() - otherPoint.getX(), 2) + Math.pow(getY() - otherPoint.getY(), 2));
	}
	
	final int walkingDistance(final Location otherPoint) {
		return Math.abs(getX() - otherPoint.getX()) + Math.abs(getY() - otherPoint.getY());
	}
	
	@Override
	public Comparator<Locatable> getDistanceComparator() {
		return new DistanceComparator();
	}
	
	@Override
	public Comparator<Locatable> getWalkingDistanceComparator() {
		return new WalkingDistanceComparator();
	}
	
	@Override
	public final Location getLocation() {
		return this;
	}
	
	@Override
	public String toString() {
		return "[ " + getSpace() + " | " + getX() + ", " + getY() + ", " + getZ() + "]";
	}
	
	@Override
	public Location clone() {
		return getSpace().createLocation(getX(), getY(), getZ());
	}
	
	protected Location() {
		
	}
	
}
