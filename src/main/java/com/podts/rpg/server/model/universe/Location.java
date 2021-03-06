package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A 3D point in a {@link Space}. This class is immutable in the sense that for any given Location for the instances entire life
 * it will always return the same coordinates and plane.
 * @author David
 *
 */
public abstract class Location implements Shiftable<Location>, Cloneable {
	
	public static final Location validate(Location location) {
		if(location == null)
			return Space.NOWHERE;
		return location;
	}
	
	public static final double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
	}
	
	public static final int walkingDistance(int x1, int y1, int x2, int y2) {
		return Math.max(x2-x1, y2-y1);
	}
	
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
		private static final Direction[] basicVals = new Direction[4];
		private static final Direction[] diagVals = new Direction[4];
		private static final List<Direction> all = Collections.unmodifiableList(Arrays.asList(vals));
		private static final List<Direction> basicAll;
		private static final List<Direction> diagAll;
		
		static {
			basicAll = Collections.unmodifiableList(Arrays.asList(basicVals));
			diagAll = Collections.unmodifiableList(Arrays.asList(diagVals));
			int i = 0;
			for(Direction d : vals) {
				if(d.isDiagonal())
					diagVals[i++] = d;
				else
					basicVals[i] = d;
			}
		}
		
		public static Collection<Direction> getAll() {
			return all;
		}
		
		public static Collection<Direction> getBasics() {
			return basicAll;
		}
		
		public static Collection<Direction> getDiagonals() {
			return diagAll;
		}
		
		public static Stream<Direction> all() {
			return getAll().stream();
		}
		
		public static Stream<Direction> basics() {
			return getBasics().stream();
		}
		
		public static Stream<Direction> diagonals() {
			return getDiagonals().stream();
		}
		
		public static Collection<Direction> sortedDistance(Location from, Location to) {
			Queue<Direction> result = new PriorityQueue<Direction>((a,b) -> {
				return (int)(distance(from.getX() + a.getX(), from.getY() + a.getX(), to.getX(), to.getY())
						- distance(from.getX() + b.getX(), from.getY() + b.getX(), to.getX(), to.getY()));
			});
			result.addAll(getAll());
			return result;
		}
		
		public static Collection<Direction> sortedDistance(HasLocation from, HasLocation to) {
			return sortedDistance(from.getLocation(), to.getLocation());
		}
		
		public static Optional<Direction> get(HasLocation from, HasLocation to) {
			return get(from.getLocation(), to.getLocation());
		}
		
		public static Optional<Direction> get(int dx, int dy) {
			if((Math.abs(dx) > 0 && Math.abs(dy) > 0)) {
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
		
		public static Optional<Direction> get(Location first, Location second) {
			int dx = second.getX() - first.getX();
			int dy = second.getY() - first.getY();
			return get(dx, dy);
		}
		
		private final Vector vector;
		private final boolean isDiagonal;
		
		public final Vector asVector() {
			return vector;
		}
		
		public int getX(int distance) {
			return getX() * distance;
		}
		
		public int getX() {
			return asVector().getX();
		}
		
		public int getY(int distance) {
			return getY() * distance;
		}
		
		public int getY() {
			return asVector().getY();
		}
		
		public final boolean isDiagonal() {
			return isDiagonal;
		}
		
		public Direction left(int amount) {
			return convert(RelationalDirection.LEFT, amount);
		}
		
		public Direction left() {
			return left(1);
		}
		
		public Direction right(int amount) {
			return convert(RelationalDirection.RIGHT, amount);
		}
		
		public Direction right() {
			return right(1);
		}
		
		public final Direction opposite() {
			return convert(RelationalDirection.BACKWARD);
		}
		
		public final Direction convert(RelationalDirection d, int i) {
			return d.convert(this, i);
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
			this.vector = new Vector(dx, dy);
			isDiagonal = dx != 0 && dy != 0;
		}
		
	}
	
	public enum RelationalDirection {
		
		FORWARD((d,i) -> d),
		BACKWARD((d,i) -> Direction.vals[(d.ordinal() + 4) % Direction.vals.length] ),
		LEFT((d,i) -> Direction.vals[(d.ordinal() + i) % Direction.vals.length] ),
		RIGHT((d,i) -> Direction.vals[Math.floorMod(d.ordinal() - i, Direction.vals.length)] );
		
		private static final RelationalDirection[] vals = RelationalDirection.values();
		private static final List<RelationalDirection> all = Collections.unmodifiableList(Arrays.asList(vals));
		
		public static Collection<RelationalDirection> getAll() {
			return all;
		}
		
		public static Stream<RelationalDirection> stream() {
			return getAll().stream();
		}
		
		private final BiFunction<Direction,Integer,Direction> operator;
		
		public Direction convert(Direction d, int i) {
			return operator.apply(d, i);
		}
		
		public Direction convert(Direction d) {
			return convert(d, 1);
		}
		
		public boolean turns() {
			return ordinal() > 1;
		}
		
		private RelationalDirection(BiFunction<Direction,Integer,Direction> operator) {
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
			return Collections.emptySet();
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
	
	private static final Collection<Location> doShiftLocations(Collection<Location> points, UnaryOperator<Location> op) {
		if(points.isEmpty())
			return Collections.emptySet();
		Location[] pArr = new Location[points.size()];
		int i=0;
		for(Location point : points)
			pArr[i++] = op.apply(point);
		return Arrays.asList(pArr);
	}
	
	public static final Collection<Location> shiftLocations(Collection<Location> points, int x, int y, int z) {
		Objects.requireNonNull(points, "Cannot shift null collection of Locations!");
		return doShiftLocations(points, l -> l.shift(x, y, z));
	}
	
	public static final Collection<Location> shiftLocations(Collection<Location> points, int x, int y) {
		Objects.requireNonNull(points, "Cannot shift null collection of Locations!");
		return doShiftLocations(points, l -> l.shift(x, y));
	}
	
	public static final Collection<Location> shiftLocations(Collection<Location> points, Vector vector) {
		Objects.requireNonNull(points, "Cannot shift null collection of Locations!");
		Objects.requireNonNull(vector, "Cannot shift Locations by a null Vector!");
		if(vector.isZero())
			return points;
		return doShiftLocations(points, l -> l.shift(vector));
	}
	
	public static final Collection<Location> shiftLocations(Collection<Location> points, Direction dir) {
		Objects.requireNonNull(points, "Cannot shift null collection of Locations!");
		Objects.requireNonNull(dir, "Cannot shift Locations by a null Direction!");
		return doShiftLocations(points, l -> l.shift(dir));
	}
	
	@Override
	public final boolean isNowhere() {
		return this == Space.NOWHERE;
	}
	
	@Override
	public Location getLocation() {
		return this;
	}
	
	public abstract int getX();
	public abstract int getY();
	
	public int getZ() {
		return getPlane().getZ();
	}
	
	public final int getAbsX() {
		return Math.abs(getX());
	}
	
	public final int getXDiff(Location other) {
		return getX() - other.getX();
	}
	
	public final int getAbsY() {
		return Math.abs(getY());
	}
	
	public final int getYDiff(Location other) {
		return getY() - other.getY();
	}
	
	public final int getAbsZ() {
		return Math.abs(getZ());
	}
	
	public final int getZDiff(Location other) {
		return getZ() - other.getZ();
	}
	
	public Vector asVector() {
		return new Vector(getX(), getY(), getZ());
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
	public Tile getTile() {
		return getSpace().getTile(this).orElse(null);
	}
	
	public final boolean tileExists() {
		return getTile() != null;
	}
	
	@Override
	public final boolean occupies(HasLocation loc) {
		return equals(loc.getLocation());
	}
	
	@Override
	public Space getSpace() {
		return getPlane().getSpace();
	}
	
	@Override
	public abstract Plane getPlane();
	
	public Vector getDifference(Location other) {
		return new Vector(other.getX() - getX()
				, other.getY() - getY()
				, other.getZ() - getZ());
	}
	
	public Stream<Entity> entities() {
		return getSpace().entities(this);
	}
	
	public final double distance(final Location point) {
		return distance(getX(), getY(), point.getX(), point.getY());
	}
	
	public final double distance(final HasLocation loc) {
		return distance(loc.getLocation());
	}
	
	@Override
	public final double distance(final Locatable loc, double cutoff) {
		cutoff = Double.max(0d, cutoff);
		Collection<? extends Location> points = loc.getLocations();
		double shortest = Double.MAX_VALUE;
		for(Location p : points) {
			double length = distance(p);
			if(length <= cutoff) return length;
			if(length < shortest)
				shortest = length;
		}
		return shortest;
	}
	
	public final int walkingDistance(final Location point) {
		return walkingDistance(getX(), getY(), point.getX(), point.getY());
	}
	
	public final int walkingDistance(final HasLocation loc) {
		return walkingDistance(loc.getLocation());
	}
	
	@Override
	public final int walkingDistance(final Locatable loc, int cutoff) {
		cutoff = Integer.max(0, cutoff);
		Collection<? extends Location> points = loc.getLocations();
		int shortest = Integer.MAX_VALUE;
		for(Location p : points) {
			int length = walkingDistance(p);
			if(length <= cutoff) return length;
			if(length < shortest)
				shortest = length;
		}
		return shortest;
	}
	
	public final boolean isBetween(Location point, double innerRadius, double outerRadius) {
		double d = distance(point);
		return d >= innerRadius && d <= outerRadius;
	}
	
	public final <L extends Location> Comparator<L> getDistanceComparator() {
		return (a,b) -> (int)(distance(a) - distance(b));
	}
	
	public final <L extends Location> Comparator<L> getWalkingDistanceComparator() {
		return (a,b) -> walkingDistance(a) - walkingDistance(b);
	}
	
	@Override
	public String toString() {
		return "[" + getSpace() + " | " + getX() + ", " + getY() + ", " + getZ() + "]";
	}
	
	@Override
	public Location clone() {
		return this;
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
		int hash = getSpace().hashCode();
		hash = hash * 31 + getX();
		hash = hash * 31 + getY();
		hash = hash * 31 + getZ();
		return hash;
	}
	
	protected Location() {
		
	}
	
}
