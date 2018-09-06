package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class Location implements Spatial, Cloneable {
	
	public enum Direction {
		UP(0,-1),
		LEFT(-1,0),
		DOWN(0,1),
		RIGHT(1,0);
		
		private static final Direction[] vals = Direction.values();
		private static final List<Direction> all = Collections.unmodifiableList(Arrays.asList(vals));
		
		public static Collection<Direction> all() {
			return all;
		}
		
		public static final Stream<Direction> stream() {
			return all().stream();
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
			if(dx != 0 && dy != 0) return null;
			dx = Integer.signum(dx);
			dy = Integer.signum(dy);
			for(Direction d : vals) {
				if(d.getX() == dx && d.getY() == dy)
					return d;
			}
			return null;
		}
		
		private final int dx, dy;
		
		public int getX(int distance) {
			return dx * distance;
		}
		
		public int getX() {
			return dx;
		}
		
		public int getY(int distance) {
			return dy * distance;
		}
		
		public int getY() {
			return dy;
		}
		
		public final Direction opposite() {
			return convert(RelationalDirection.BACKWARD);
		}
		
		public final Direction convert(RelationalDirection d) {
			return d.convert(this);
		}
		
		public final Location MoveFromLocation(Location origin, int distance) {
			return origin.shift(getX(distance), getY(distance));
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
		
		public boolean turns() {
			return ordinal() > 2;
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
	
	@Override
	public Location getLocation() {
		return this;
	}
	
	public final boolean planeExists() {
		return getPlane() != null;
	}
	
	public abstract int getX();
	public abstract int getY();
	
	public int getZ() {
		return getPlane().getZ();
	}
	
	public final int getXDifference(Location other) {
		return getX() - other.getX();
	}
	
	public final int getYDifference(Location other) {
		return getY() - other.getY();
	}
	
	public final int getZDifference(Location other) {
		return getZ() - other.getZ();
	}
	
	public Location shift(final int dx, final int dy, final int dz) {
		if(dz == 0)
			return shift(dx, dy);
		return getSpace().createLocation(getX() + dx, getY() + dy, getZ() + dz);
	}
	
	public Location shift(final int dx, final int dy) {
		return getPlane().createLocation(getX() + dx, getY() + dy);
	}
	
	public Location shift(Direction dir, int distance) {
		return shift(dir.getX(distance), dir.getY(distance));
	}
	
	public Location shift(Direction dir) {
		return shift(dir, 1);
	}
	
	@Override
	public Tile getTile() {
		return getSpace().getTile(this);
	}
	
	@Override
	public final boolean occupies(Spatial loc) {
		return getLocation().equals(loc.getLocation());
	}
	
	@Override
	public Plane getPlane() {
		return getLocation().getPlane();
	}
	
	public Stream<Entity> entities() {
		return getSpace().entities(this);
	}
	
	public Stream<Location> traceTo(Location other) {
		if(isInDifferentSpace(other))
			return Stream.empty();
		
		Direction dir = Direction.get(this, other);
		if(dir == null)
			return Stream.empty();
		
		return trace(dir)
				.limit(walkingDistance(other) + 1);
	}
	
	public Stream<Location> trace(Direction dir, int distance) {
		return trace(dir)
				.limit(distance + 1);
	}
	
	public Stream<Location> traceEvery(Direction dir, int increment) {
		if(dir == null) return Stream.empty();
		return Stream.iterate(this, point -> point.shift(dir, increment));
	}
	
	/**
	 * Returns an infinite Stream consisting of this point and all points in the given direction from closest to farthest in order.
	 * @param dir - The direction to shift this point.
	 * @return infinite Stream consisting of this point and all points in the given direction from closest to farthest in order
	 */
	public Stream<Location> trace(Direction dir) {
		return traceEvery(dir, 1);
	}
	
	public Stream<Location> bitrace(Direction dir, int distance) {
		return bitrace(dir)
				.limit(distance * 2 + 1);
	}
	
	public Stream<Location> bitraceEvery(Direction dir, int increment) {
		if(dir == null) return Stream.empty();
		return IntStream.iterate(0, i -> {
			i *= -1;
			if(i >= 0)
				i += increment;
			return i;
		}).mapToObj(i -> shift(dir, i));
	}
	
	public Stream<Location> bitrace(Direction dir) {
		return bitraceEvery(dir, 1);
	}
	
	final double distance(final Location otherPoint) {
		return Math.sqrt(Math.pow(getX() - otherPoint.getX(), 2) + Math.pow(getY() - otherPoint.getY(), 2));
	}
	
	final int walkingDistance(final Location otherPoint) {
		return Math.abs(getX() - otherPoint.getX()) + Math.abs(getY() - otherPoint.getY());
	}
	
	public final boolean isBetween(Location point, double innerRadius, double outerRadius) {
		double d = distance(point);
		return d >= innerRadius && d <= outerRadius;
	}
	
	@Override
	public String toString() {
		return "[" + getSpace() + " | " + getX() + ", " + getY() + ", " + getZ() + "]";
	}
	
	@Override
	public Location clone() {
		return getSpace().createLocation(getX(), getY(), getZ());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof Location) {
			Location other = (Location) o;
			return isInSameSpace(other) &&
					getX() == other.getX() &&
					getY() == other.getY() &&
					getZ() == other.getZ();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSpace(), getX(), getY(), getZ());
	}
	
	protected Location() {
		
	}
	
}
