package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class Location implements Shiftable, Cloneable {
	
	public enum Direction {
		UP(0,-1),
		TOP_LEFT(1,-1),
		LEFT(-1,0),
		BOTTOM_LEFT(-1,-1),
		DOWN(0,1),
		BOTTOM_RIGHT(1,-1),
		RIGHT(1,0),
		TOP_RIGHT(-1,1);
		
		private static final Direction[] vals = Direction.values();
		private static final Direction[] diagVals;
		private static final List<Direction> all = Collections.unmodifiableList(Arrays.asList(vals));
		private static final List<Direction> diagAll;
		
		static {
			diagVals = new Direction[4];
			diagAll = Collections.unmodifiableList(Arrays.asList(diagVals));
			int i = 0;
			for(Direction d : vals) {
				if(d.isDiagonal())
					diagVals[i++] = d;
			}
		}
		
		public static Collection<Direction> getAll() {
			return all;
		}
		
		public static Collection<Direction> getDiagonals() {
			return diagAll;
		}
		
		public static final Stream<Direction> all() {
			return getAll().stream();
		}
		
		public static final Stream<Direction> diagonals() {
			return getDiagonals().stream();
		}
		
		public static final Optional<Direction> get(HasLocation first, HasLocation second) {
			return get(first.getLocation(), second.getLocation());
		}
		
		public static final Optional<Direction> get(Location first, Location second) {
			int dx = second.getX() - first.getX();
			int dy = second.getY() - first.getY();
			
			if((Math.abs(dx) > 0 || Math.abs(dy) > 0)) {
				if(Math.abs(dx) != Math.abs(dy))
					return Optional.empty();
			}
			
			dx = Integer.signum(dx);
			dy = Integer.signum(dy);
			
			for(Direction dir : vals) {
				if(dir.getX() == dx && dir.getY() == dy)
					return Optional.of(dir);
			}
			throw new AssertionError("No direction found after filter! Method should be re-evaluated!");
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
		private final boolean isDiagonal;
		
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
		
		public final boolean isDiagonal() {
			return isDiagonal;
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
			isDiagonal = dx != 0 && dy != 0;
		}
		
	}
	
	public enum RelationalDirection {
		
		FORWARD(d -> d),
		BACKWARD(d -> Direction.vals[(d.ordinal() + 4) % Direction.vals.length] ),
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
	
	private static final BiFunction<Location,Location,Optional<Direction>> safePointsToDirection = (previous,next) -> {
		if(!previous.isInPlane(next))
			throw new IllegalArgumentException("Cannot convert points that are in different planes into Directions!");
		Optional<Direction> dir = Direction.get(previous, next);
		if(!dir.isPresent())
			throw new IllegalArgumentException("Cannot convert non-close points into Directions!");
		return dir;
	};
	
	static final Collection<Direction> mapDirections(Collection<? extends HasLocation> points
			, BiFunction<Location,Location,Optional<Direction>> dirFunction) {
		if(points.size() < 2)
			return Collections.emptyList();
		final Direction[] dirs = new Direction[points.size() - 1];
		int i = 0;
		final Iterator<? extends HasLocation> it = points.iterator();
		Location previous = it.next().getLocation();
		while(it.hasNext()) {
			Location next = it.next().getLocation();
			Direction dir = dirFunction.apply(previous,next).get();
			dirs[i++] = dir;
			previous = next;
		}
		return Arrays.asList(dirs);
	}
	
	static final Collection<Direction> doMapDirections(Collection<? extends HasLocation> points) {
		return mapDirections(points, Direction::get);
	}
	
	public static final Collection<Direction> mapDirections(Collection<? extends HasLocation> points) {
		return mapDirections(points, safePointsToDirection);
	}
	
	@SafeVarargs
	public static final <L extends HasLocation> Collection<Direction> mapDirections(L... points) {
		return mapDirections(Arrays.asList(points));
	}
	
	@Override
	public final boolean isNowhere() {
		return this == Space.NOWHERE;
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
	
	public final int getAbsX() {
		return Math.abs(getX());
	}
	
	public final int getXDifference(Location other) {
		return getX() - other.getX();
	}
	
	public final int getAbsY() {
		return Math.abs(getY());
	}
	
	public final int getYDifference(Location other) {
		return getY() - other.getY();
	}
	
	public final int getAbsZ() {
		return Math.abs(getZ());
	}
	
	public final int getZDifference(Location other) {
		return getZ() - other.getZ();
	}
	
	@Override
	public Location shift(int dx, int dy, int dz) {
		if(dz == 0)
			return shift(dx, dy);
		return getSpace().createLocation(getX() + dx, getY() + dy, getZ() + dz);
	}
	
	@Override
	public Location shift(int dx, int dy) {
		return getPlane().createLocation(getX() + dx, getY() + dy);
	}
	
	@Override
	public Location shift(Direction dir, int distance) {
		return shift(dir.getX(distance), dir.getY(distance));
	}
	
	@Override
	public Location shift(Direction dir) {
		return shift(dir, 1);
	}
	
	@Override
	public Tile getTile() {
		return getSpace().getTile(this);
	}
	
	@Override
	public final boolean occupies(HasLocation loc) {
		return equals(loc.getLocation());
	}
	
	@Override
	public Plane getPlane() {
		return getLocation().getPlane();
	}
	
	public Stream<Entity> entities() {
		return getSpace().entities(this);
	}
	
	public Stream<? extends Location> traceTo(Location other) {
		if(isInDifferentSpace(other))
			return Stream.empty();
		
		Optional<Direction> dir = Direction.get(this, other);
		if(!dir.isPresent())
			return Stream.empty();
		
		return trace(dir.get())
				.limit(walkingDistance(other) + 1);
	}
	
	public Stream<? extends Location> trace(Direction dir, int distance) {
		return trace(dir)
				.limit(distance + 1);
	}
	
	public Stream<? extends Location> traceEvery(Direction dir, int increment) {
		if(dir == null)
			return Stream.empty();
		return Stream.iterate(this, point -> point.shift(dir, increment));
	}
	
	/**
	 * Returns an infinite Stream consisting of this point and all points in the given direction from closest to farthest in order.
	 * @param dir - The direction to shift this point.
	 * @return infinite Stream consisting of this point and all points in the given direction from closest to farthest in order
	 */
	public Stream<? extends Location> trace(Direction dir) {
		return traceEvery(dir, 1);
	}
	
	public Stream<? extends Location> bitrace(Direction dir, int distance) {
		return bitrace(dir)
				.limit(distance * 2 + 1);
	}
	
	public Stream<? extends Location> bitraceEvery(Direction dir, int increment) {
		if(dir == null)
			return Stream.empty();
		return IntStream.iterate(0, i -> {
			i *= -1;
			if(i >= 0)
				i += increment;
			return i;
		}).mapToObj(i -> shift(dir, i));
	}
	
	public Stream<? extends Location> bitrace(Direction dir) {
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
			return isInSameSpace(other)
					&& getX() == other.getX()
					&& getY() == other.getY()
					&& getZ() == other.getZ();
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
