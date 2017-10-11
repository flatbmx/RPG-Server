package com.podts.rpg.server.model.universe.region;


import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.World;

public final class Regions {
	
	/**
	 * Returns the Universal Region.
	 * @return A region that contains all non-null points.
	 */
	public static final Region getUniversalRegion() {
		return universalRegion;
	}
	
	/**
	 * The Universal Set that contains all points except null;
	 */
	private static final Region universalRegion = new IncompleteRegion() {
		@Override
		public boolean contains(final Locatable loc) {
			return loc != null;
		}
	};
	
	public static final Region getEmptyRegion() {
		return emptyRegion;
	}
	
	private static final Region emptyRegion = new IncompleteRegion() {
		@Override
		public boolean contains(final Locatable loc) {
			return false;
		}
	};
	
	private final static Set<Location> emptyPointSet = Collections.unmodifiableSet(new HashSet<Location>());
	private static final StaticSetRegion emptySetRegion = new StaticSetRegion(emptyPointSet);
	
	public static final SetRegion getEmptySetRegion() {
		return emptySetRegion;
	}
	
	//TODO create new empty for a specific world.
	private static final RectangularRegion emptyRectangularRegion = new RectangularRegion() {

		@Override
		public Location getCenter() {
			return null;
		}

		@Override
		public Collection<RegionListener> getRegionListeners() {
			return null;
		}

		@Override
		public Region addRegionListener(RegionListener newRegionListener) {
			return null;
		}

		@Override
		public Region removeRegionListener(RegionListener regionListener) {
			return null;
		}

		@Override
		public List<Location> getCorners() {
			return null;
		}

		@Override
		public int getXWidth() {
			return 0;
		}

		@Override
		public int getYWidth() {
			return 0;
		}
		
	};
	
	public static final RectangularRegion getEmptyRectangularRegion() {
		return emptyRectangularRegion;
	}
	
	//TODO create new empty for a specific world.
	private static final CircularRegion emptyCircularRegion = new CircularRegion() {

		@Override
		public Location getCenter() {
			return null;
		}

		@Override
		public final Collection<RegionListener> getRegionListeners() {
			throw new UnsupportedOperationException();
		}

		@Override
		public final Region addRegionListener(RegionListener newRegionListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public final Region removeRegionListener(RegionListener regionListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getRadius() {
			return 0;
		}
		
	};
	
	public static final CircularRegion getEmptyCircularRegion() {
		return emptyCircularRegion;
	}
	
	//TODO create new empty for a specific world.
	private static final TorusRegion emptyTorusRegion = new TorusRegion() {

		@Override
		public Location getCenter() {
			return null;
		}

		@Override
		public final Collection<RegionListener> getRegionListeners() {
			throw new UnsupportedOperationException();
		}

		@Override
		public final Region addRegionListener(RegionListener newRegionListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public final Region removeRegionListener(RegionListener regionListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getOuterRadius() {
			return 0;
		}

		@Override
		public int getInnerRadius() {
			return 0;
		}
		
	};
	
	public static final TorusRegion getEmptyTorusRegion() {
		return emptyTorusRegion;
	}
	
	@SafeVarargs
	private static final <E> boolean containsNonNullElements(E... objects) {
		if(objects == null || objects.length == 0) return false;
		for(E o : objects) {
			if(o != null) return true;
		}
		return false;
	}
	
	private static final <E> boolean containsNonNullElements(Iterable<E> objects) {
		if(objects == null) return false;
		for(E o : objects) {
			if(o != null) return true;
		}
		return false;
	}
	
	@SafeVarargs
	private static final <U, V extends U> boolean checkNonEmptyWithCondition(final Predicate<U> condition, final V... objects) {
		if(objects == null || objects.length == 0) return false;
		boolean empty = true;
		for(final V o : objects) {
			if(o == null) continue;
			empty = false;
			if(!condition.test(o)) return false;
		}
		return !empty;
	}
	
	private static final <U, V extends U> boolean checkNonEmptyWithCondition(final Predicate<U> condition, final Iterable<V> objects) {
		if(objects == null) return false;
		boolean empty = true;
		for(final V o : objects) {
			if(o == null) continue;
			empty = false;
			if(!condition.test(o)) return false;
		}
		return !empty;
	}
	
	@SafeVarargs
	public static final <R extends Region> boolean isStaticRegion(final R... regions) {
		return checkNonEmptyWithCondition((final R r) -> !(r instanceof DynamicRegion), regions);
	}
	
	public static final <R extends Region> boolean isStaticRegion(final Iterable<R> regions) {
		return checkNonEmptyWithCondition((final R r) -> !(r instanceof DynamicRegion), regions);
	}
	
	@SafeVarargs
	public static final <L extends Locatable> boolean isStaticLocatable(final L... locs) {
		return checkNonEmptyWithCondition((final L l) -> l instanceof Location, locs);
	}
	
	public static final <L extends Locatable> boolean isStaticLocatable(final Iterable<L> locs) {
		return checkNonEmptyWithCondition((final L l) -> l instanceof Location, locs);
	}
	
	@SafeVarargs
	public static final <R extends Region> boolean arePollable(final R... regions) {
		return checkNonEmptyWithCondition((final R r) -> r instanceof PollableRegion, regions);
	}
	
	public static final <R extends Region> boolean arePollable(final Iterable<R> regions) {
		return checkNonEmptyWithCondition((final R r) -> r instanceof PollableRegion, regions);
	}
	
	@SafeVarargs
	public static final <R extends Region> boolean areCircular(final R... regions) {
		return checkNonEmptyWithCondition((final R r) -> r instanceof CircularRegion || resemblesCircular(r), regions);
	}
	
	public static final <R extends Region> boolean areCircular(final Iterable<R> regions) {
		return checkNonEmptyWithCondition((final R r) -> r instanceof CircularRegion || resemblesCircular(r), regions);
	}
	
	@SafeVarargs
	public static final boolean areCircular(final Set<Location>... pointSets) {
		if(pointSets == null || pointSets.length == 0) return false;
		for(final Set<Location> points : pointSets) {
			if(points == null) continue;
			if(!computeResemblesCircular(points)) return false;
		}
		return true;
	}
	
	private static final boolean resemblesCircular(final Region r) {
		if((r instanceof CircularRegion)) return true;
		
		final Set<Location> points;
		if(r instanceof PollableRegion)
			points = ((PollableRegion) r).getPoints();
		else
			points = findPoints(r);
		
		return computeResemblesCircular(points);
	}
	
	private static final boolean computeResemblesCircular(final Set<Location> points) {
		return computeCircularParameters(points) != null;
	}
	
	private static final Entry<Location,Integer> computeCircularParameters(final Set<Location> points) {
		if(isDisjoint(points)) return null;
		
		int radius = 0;
		Location center = computeFindPlaneCenter(points);
		if(center == null) return null;
		for(final Location point : points) {
			int nr = (int) center.distance(point);
			if(nr > radius) radius = nr;
		}
		
		final Set<Location> circlePoints = new StaticCircularRegion(center, radius).getPoints();
		for(final Location truePoint : circlePoints) {
			if(!points.contains(truePoint)) return null;
		}
		
		return new AbstractMap.SimpleEntry<Location, Integer>(center, radius);
	}
	
	public static final <R extends Region> boolean areTorus(final Iterable<R> regions) {
		return checkNonEmptyWithCondition((R r) -> resemblesTorus(r), regions);
	}
	
	private static final <R extends Region> boolean resemblesTorus(final R r) {
		if((r instanceof TorusRegion)) return true;
		
		final Set<Location> points;
		if(r instanceof PollableRegion)
			points = ((PollableRegion) r).getPoints();
		else
			points = findPoints(r);
		
		return computeResemblesTorus(points);
	}
	
	private static final boolean computeResemblesTorus(final Set<Location> points) {
		if(points.isEmpty()) return false;
		
		final Location center = computeFindPlaneCenter(points);
		if(center == null) return false;
		
		int minRadius = Integer.MAX_VALUE, maxRadius = 0;
		for(final Location point : points) {
			int distance = (int) center.distance(point);
			if(distance < minRadius) minRadius = distance;
			if(distance > maxRadius) maxRadius = distance;
		}
		
		//If the center is within the region it cannot be a torus.
		if(minRadius == 0 || maxRadius == 0) return false;
		
		final Set<Location> torusPoints = new StaticTorusRegion(center, maxRadius, minRadius).getPoints();
		for(final Location truePoint : torusPoints) {
			if(!points.contains(truePoint)) return false;
		}
		
		return true;
	}
	
	public static final boolean areRectangular(final Region... regions) {
		return checkNonEmptyWithCondition((Region r) -> resemblesRectangle(r), regions);
	}
	
	public static final <R extends Region> boolean areRectangular(final Iterable<R> regions) {
		return checkNonEmptyWithCondition((R r) -> resemblesRectangle(r), regions);
	}
	
	private static final boolean resemblesRectangle(final Region r) {
		if((r instanceof RectangularRegion)) return true;
		
		final Set<Location> points;
		if(r instanceof PollableRegion)
			points = ((PollableRegion) r).getPoints();
		else
			points = findPoints(r);
		
		return computeResemblesRectangle(points);
	}
	
	private static final boolean computeResemblesRectangle(final Set<Location> points) {
		if(points.size() < 4) return false;
		
		final Iterator<Location> it = points.iterator();
		Location topLeft = it.next();
		Location bottomRight = it.next();
		if(bottomRight.getX() <= topLeft.getX() && bottomRight.getY() <= topLeft.getY()) {
			Location temp = topLeft;
			topLeft = bottomRight;
			bottomRight = temp;
		}
		for(Location point = it.next(); it.hasNext(); point = it.next()) {
			if(point.getX() <= topLeft.getX() && point.getY() <= topLeft.getY()) topLeft = point;
			else if(point.getX() >= bottomRight.getX() && point.getY() >= bottomRight.getY()) bottomRight = point;
		}
		
		int width = bottomRight.getX() - topLeft.getX();
		int height = bottomRight.getY() - topLeft.getY();
		return points.size() == width*height;
	}
	
	@SafeVarargs
	public static final <R extends Region> Region union(final R... regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		if(arePollable(regions))
			return new PollableUnionRegion<PollableRegion>((PollableRegion[]) regions);
		else
			return new UnionRegion<R>(regions);
	}
	
	@SuppressWarnings("unchecked")
	public static final <R extends Region> Region union(final Iterable<R> regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		if(arePollable(regions))
			return new PollableUnionRegion<PollableRegion>((Iterable<PollableRegion>)regions);
		else
			return new UnionRegion<R>(regions);
	}
	
	@SafeVarargs
	public static final <R extends Region> Region intersect(final R... regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		if(arePollable(regions)) {
			if(isStaticRegion(regions)) return new StaticPollableIntersectRegion(regions);
			return new PollableIntersectRegion<PollableRegion>((PollableRegion[]) regions);
		} else
			return new IntersectRegion(regions);
	}
	
	@SafeVarargs
	public static final <R extends PollableRegion> Region intersect(final R... regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		if(arePollable(regions)) {
			if(isStaticRegion(regions)) return new StaticPollableIntersectRegion(regions);
			return new PollableIntersectRegion<PollableRegion>( regions);
		} else
			return new IntersectRegion(regions);
	}
	
	@SuppressWarnings("unchecked")
	public static final <R extends Region> Region intersect(final Iterable<R> regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		if(arePollable(regions))
			return new PollableIntersectRegion<PollableRegion>((Iterable<PollableRegion>)regions);
		else
			return new IntersectRegion(regions);
	}
	
	@SafeVarargs
	public static final <R extends Region> Region difference(final Region initial, final R... removed) {
		if(initial == null) return getEmptyRegion();
		if(!containsNonNullElements(removed)) return initial;
		if(arePollable(initial) && arePollable(removed))
			return new PollableComplementRegion<PollableRegion>((PollableRegion)initial, (PollableRegion[]) removed);
		return new DifferenceRegion(initial, removed);
	}
	
	@SuppressWarnings("unchecked")
	public static final Region difference(final Region initial, final Iterable<? extends Region> removed) {
		if(initial == null) return getEmptyRegion();
		if(!containsNonNullElements(removed)) return initial;
		if(arePollable(initial) && arePollable(removed))
			return new PollableComplementRegion<PollableRegion>((PollableRegion)initial, (Iterable<PollableRegion>)removed);
		return new DifferenceRegion(initial, removed);
	}
	
	public static final Region complement(final Region... regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		return new ComplementRegion(regions);
	}
	
	public static final Region complement(final Iterable<Region> regions) {
		if(!containsNonNullElements(regions)) return getEmptyRegion();
		return new ComplementRegion(regions);
	}
	
	public static final boolean isDisjoint(final Set<Location> points) {
		if(points == null) return false;
		final Set<Location> leftOverPoints = new HashSet<Location>(points);
		final Queue<Location> pointQueue = new LinkedList<Location>();
		final Location start = leftOverPoints.iterator().next();
		pointQueue.add(start);
		leftOverPoints.remove(start);
		while(!pointQueue.isEmpty()) {
			final Location point = pointQueue.poll();
			for(int dy=-1; dy < 2; ++dy) {
				for(int dx=-1; dx < 2; ++dx) {
					final Location newPoint = point.move(dx, dy, 0);
					if(leftOverPoints.remove(newPoint)) {
						pointQueue.add(newPoint);
					}
				}
			}
		}
		return !leftOverPoints.isEmpty();
	}
	
	public static final Set<Set<Location>> getDisjointSets(final PollableRegion... regions) {
		if(!containsNonNullElements(regions)) return new HashSet<Set<Location>>();
		final Set<Location> points = new HashSet<Location>();
		for(final PollableRegion r : regions) {
			points.addAll(r.getPoints());
		}
		return computeDisjointSets(points);
	}
	
	public static final Set<Set<Location>> getDisjointSets(final Region... regions) {
		if(!containsNonNullElements(regions)) return new HashSet<Set<Location>>();
		final Set<Location> points = new HashSet<Location>();
		if(!arePollable(regions)) return new HashSet<Set<Location>>();
		for(final Region r : regions) {
			points.addAll(((PollableRegion)r).getPoints());
		}
		return computeDisjointSets(points);
	}
	
	public static final Set<Set<Location>> getDisjointSets(final Set<Location> points) {
		if(points == null) return new HashSet<Set<Location>>();
		return computeDisjointSets(new HashSet<Location>(points));
	}
	
	private static final Set<Set<Location>> computeDisjointSets(final Set<Location> leftOverPoints) {
		final Set<Set<Location>> result = new HashSet<Set<Location>>();
		final Queue<Location> pointQueue = new LinkedList<Location>();
		while(!leftOverPoints.isEmpty()) {
			final Set<Location> island = new HashSet<Location>();
			final Location start = leftOverPoints.iterator().next();
			pointQueue.add(start);
			leftOverPoints.remove(start);
			while(!pointQueue.isEmpty()) {
				final Location point = pointQueue.poll();
				island.add(point);
				for(int dy=-1; dy < 2; ++dy) {
					for(int dx=-1; dx < 2; ++dx) {
						final Location newPoint = point.move(dx, dy, 0);
						if(leftOverPoints.remove(newPoint)) {
							pointQueue.add(newPoint);
						}
					}
				}
			}
			result.add(island);
		}
		return result;
	}
	
	public static final Set<Location> getEdges(final Set<Location> points) {
		final Set<Set<Location>> islands = getDisjointSets(points);
		final Set<Location> result = new HashSet<Location>();
		
		for(final Set<Location> island : islands) {
			for(final Location point : island) {
				for(int dx=-1; dx<=1; ++dx) {
					for(int dy=-1; dy<=1; ++dy) {
						final Location newPoint = point.move(dx, dy, 0);
						if(!points.contains(newPoint)) result.add(newPoint);
					}
				}
			}
		}
		
		return result;
	}
	
	public static final List<Location> getVertexs(final Set<Location> edges) {
		return computeGetVertexs(new HashSet<Location>(edges));
	}
	
	private static final List<Location> computeGetVertexs(final Set<Location> edges) {
		final List<Location> result = new LinkedList<Location>();
		if(edges == null || edges.size() < 2) return result;
		
		final Iterator<Location> it = edges.iterator();
		Location lastPoint = it.next();
		Location point = lastPoint;
		int dirX = 0, dirY = 0;
		
		do {
			boolean found = false;
			//Find direction
			for(int dx=-1; dx<=1; ++dx) {
				for(int dy=-1; dy<=1; ++dy) {
					if(dx == 0 && dy == 0) continue;
					final Location possiblePoint = point.move(dx, dy, 0);
					if(edges.contains(possiblePoint)) {
						dirX = dx;
						dirY = dy;
						point = possiblePoint;
						edges.remove(point);
						found = true;
						break;
					}
				}
				if(found) break;
			}
			if(dirX == 0 && dirY == 0) return result;
			
			do{
				point = point.move(dirX, dirY, 0);
				edges.remove(point);
			} while(edges.contains(point));
			result.add(point);
		} while(!edges.isEmpty());
		
		return result;
	}
	
	public static final Set<Location> findPoints(final Region r) {
		if(r == null) return new HashSet<Location>();
		if(r instanceof PollableRegion) return ((PollableRegion) r).getPoints();
		final Set<Location> points = new HashSet<Location>();
		computeFindPoints(r, points);
		return points;
	}
	
	public static final Set<Location> findPoints(final Region r, Set<Location> points) {
		if(points == null) points = new HashSet<Location>();
		if(r == null) return points;
		if(r instanceof PollableRegion) {
			points.addAll(((PollableRegion)r).getPoints());
			return points;
		}
		computeFindPoints(r, points);
		return points;
	}
	
	private static final void computeFindPoints(final Region r, final Set<Location> points) {
		//TODO implement me
		/*final Set<MapRegionPoller> pollers = new HashSet<MapRegionPoller>();
		for(final MapRegion mapRegion : World.getWorld().getRegionManager().getAllRegions()) {
			pollers.add(new MapRegionPoller(mapRegion));
		}
		pollers.parallelStream().forEach((poller) -> poller.findPoints(r));
		for(final MapRegionPoller poller : pollers) {
			points.addAll(poller.pointsFound);
		}*/
	}
	
	public static final Location findPlaneCenter(final Set<? extends Locatable> locs) {
		Objects.requireNonNull(locs);
		return computeFindPlaneCenter(locs);
	}
	
	private static final <L extends Locatable> Location computeFindPlaneCenter(final Set<L> locs) {
		long ax = 0, ay = 0;
		int z;
		final Iterator<L> it = locs.iterator();
		final L first = it.next();
		Location point = first.getLocation();
		World world = point.getWorld();
		ax = point.getX();
		ay = point.getY();
		z = point.getZ();
		
		if(it.hasNext()) {
			for(L loc = it.next(); it.hasNext(); loc = it.next()) {
				if(loc == null) continue;
				point = loc.getLocation();
				ax += point.getX();
				ay += point.getY();
				if(point.getZ() != z) return null;
			}
		}
		
		return world.createLocation((int)ax/locs.size(), (int)ay/locs.size(), z);
	}
	
	public static final Location findCenter(final Set<? extends Locatable> locs) {
		Objects.requireNonNull(locs);
		return computeFindCenter(locs);
	}
	
	@SafeVarargs
	public static final <L extends Locatable> Location findCenter(final L... locs) {
		Objects.requireNonNull(locs);
		int ax = 0, ay = 0, az = 0, counter = 0;
		World world = null;
		for(final L loc : locs) {
			if(loc == null) continue;
			++counter;
			final Location l = loc.getLocation();
			world = l.getWorld();
			ax += l.getX();
			ay += l.getY();
			az += l.getZ();
		}
		if(counter == 0) throw new IllegalArgumentException("Cannot find the center of empty or all null array.");
		return world.createLocation(ax/counter, ay/counter, az/counter);
	}
	
	private static final Location computeFindCenter(final Set<? extends Locatable> locs) {
		long ax = 0, ay = 0, az = 0;
		if(locs.isEmpty() || (locs.size() == 1) && locs.contains(null)) new IllegalArgumentException("Cannot find the center of null, empty or just null containing set.");
		World world = null;
		for(final Locatable loc : locs) {
			final Location point = loc.getLocation();
			world = point.getWorld();
			ax += point.getX();
			ay += point.getY();
			az += point.getZ();
		}
		return world.createLocation((int)ax/locs.size(), (int)ay/locs.size(), (int)az/locs.size());
	}
	
	public static final boolean isProperSubSet(final PollableRegion a, final PollableRegion b) {
		if(b == null) return false;
		if(a == null) return true;
		final Set<Location> aPoints = a.getPoints();
		for(final Location point : aPoints) {
			if(!b.contains(point)) return false;
		}
		return b.getPoints().size() > aPoints.size();
	}
	
	public static final boolean isProperSubSet(final Set<Location> points, final Region region) {
		if(region == null) return false;
		if(points.isEmpty()) return true;
		return true;
	}
	
	public static final boolean isProperSubSet(final Set<Location> a, final Set<Location> b) {
		if(b == null) return false;
		if(a == null) return true;
		for(final Location point : a) {
			if(!b.contains(point)) return false;
		}
		return b.size() > a.size();
	}
	
	public static final boolean isSubSet(final PollableRegion a, final PollableRegion b) {
		if(a == null) return true;
		for(final Location point : a) {
			if(!b.contains(point)) return false;
		}
		return true;
	}
	
	public static final boolean isSubSet(final Set<Location> a, final Set<Location> b) {
		if(a == null) return true;
		for(final Location point : a) {
			if(!b.contains(point)) return false;
		}
		return true;
	}
	
	public static final Region constructRegion(final Iterable<? extends Locatable> locs) {
		if(!containsNonNullElements(locs)) return getEmptyRegion();
		return constructRegion(locs, !isStaticLocatable(locs));
	}
	
	public static final Region constructRegion(final Iterable<? extends Locatable> locs, final boolean dynamic) {
		if(!containsNonNullElements(locs)) return getEmptyRegion();
		//TODO implement
		if(dynamic) {
			return new DynamicSetRegion(locs);
		} else {
			final Set<Location> pointSet = new HashSet<Location>();
			for(final Locatable loc : locs) {
				pointSet.add(loc.getLocation());
			}
			Region r = constructCircularRegion(pointSet);
			if(r != null) return r;
			
			return new StaticSetRegion(locs);
		}
	}
	
	public static final CircularRegion constructCircularRegion(final Set<Location> points) {
		Entry<Location,Integer> values = computeCircularParameters(points);
		if(values == null) return null;
		return new StaticCircularRegion(values.getKey(),values.getValue());
	}
	
	public static final CircularRegion constructCircularRegion(final Locatable center, final int radius) {
		return constructCircularRegion(center, radius, !isStaticLocatable(center));
	}
	
	public static final CircularRegion constructCircularRegion(final Locatable center, final int radius, final boolean dynamic) {
		if(center == null || (radius == 0 && !dynamic)) return getEmptyCircularRegion();
		if(dynamic) return new DynamicCircularRegion(center, radius);
		return new StaticCircularRegion(center, radius);
	}
	
	public static TorusRegion constructTorusRegion(final Locatable center, int outerRadius, int innerRadius) {
		return constructTorusRegion(center, outerRadius, innerRadius, !isStaticLocatable(center));
	}
	
	public static final TorusRegion constructTorusRegion(final Locatable center, int outerRadius, int innerRadius, final boolean dynamic) {
		if(center == null || (!dynamic && outerRadius == 0)) return getEmptyTorusRegion();
		if(dynamic) {
			//TODO implement dynamic torus region.
		}
		return new StaticTorusRegion(center, outerRadius, innerRadius);
	}
	
	public static final RectangularRegion constructRectangularRegion(final Locatable a, final Locatable b) {
		return constructRectangularRegion(a, b, !isStaticLocatable(a,b));
	}
	
	public static final RectangularRegion constructRectangularRegion(final Locatable a, final Locatable b, final boolean dynamic) {
		if(a == null || b == null) return getEmptyRectangularRegion();
		if(dynamic) return new DynamicRectangularRegion(a,b);
		return new StaticRectangularRegion(a,b);
	}
	
	public static final SetRegion constructSetRegion(final Locatable... points) {
		return constructSetRegion(!isStaticLocatable(points), points);
	}
	
	public static final SetRegion constructSetRegion(final boolean dynamic, final Locatable... points) {
		if(!containsNonNullElements(points) && !dynamic) return getEmptySetRegion();
		if(dynamic) {
			SetRegion result = new DynamicSetRegion();
			for(Locatable loc : points) {
				result.addPoint(loc);
			}
			return result;
		}
		return new StaticSetRegion(points);
	}
	
	public static final SetRegion constructSetRegion(final Iterable<? extends Locatable> locs) {
		return constructSetRegion(locs, isStaticLocatable(locs));
	}
	
	public static final SetRegion constructSetRegion(final Iterable<? extends Locatable> locs, final boolean dynamic) {
		if(!containsNonNullElements(locs) && !dynamic) return getEmptySetRegion();
		if(dynamic) return new DynamicSetRegion(locs);
		return new StaticSetRegion(locs);
	}
	
	private static class StaticPollableIntersectRegion extends SimpleRegion implements PollableRegion {
		
		private final Set<Location> points;
		
		@Override
		public boolean contains(final Locatable loc) {
			return points.contains(loc);
		}

		@Override
		public Set<Location> getPoints() {
			return points;
		}
		
		private <R extends Region> StaticPollableIntersectRegion(final Iterable<R> newRegions) {
			final Set<Location> nPoints = new HashSet<Location>();
			
			for(final R r : newRegions) {
				if(r == null) continue;
				nPoints.addAll(((PollableRegion) r).getPoints());
			}
			nPoints.remove(null);
			
			points = Collections.unmodifiableSet(nPoints);
		}
		
		@SafeVarargs
		private <R extends Region> StaticPollableIntersectRegion(final R... newRegions) {
			final Set<Location> nPoints = new HashSet<Location>();
			
			for(final R r : newRegions) {
				if(r == null) continue;
				nPoints.addAll(((PollableRegion) r).getPoints());
			}
			
			points = Collections.unmodifiableSet(nPoints);
		}
		
	}
	
	private static class PollableIntersectRegion<R extends PollableRegion> extends CompoundRegion<R> implements PollableRegion {
		
		@Override
		public boolean contains(final Locatable loc) {
			if(loc == null) return false;
			for(final Region r : subRegions) {
				if(!r.contains(loc)) return false;
			}
			return true;
		}

		@Override
		public Set<Location> getPoints() {
			final Set<Location> newPoints = new HashSet<Location>();
			for(final PollableRegion r : subRegions) {
				newPoints.addAll(r.getPoints());
			}
			newPoints.parallelStream().filter(new Predicate<Location>() {
				@Override
				public boolean test(final Location loc) {
					for(final Region r : subRegions) {
						if(!r.contains(loc)) {
							return false;
						}
					}
					return true;
				}
			}).sequential().collect(Collectors.toCollection(() -> newPoints));
			return Collections.unmodifiableSet(newPoints);
		}
		
		@SafeVarargs
		PollableIntersectRegion(final R... regions) {
			super(regions);
		}
		
		PollableIntersectRegion(final Iterable<R> regions) {
			super(regions);
		}
		
	}
	
	private static class IntersectRegion extends CompoundRegion<Region> {
		
		@Override
		public boolean contains(final Locatable loc) {
			for(final Region r : subRegions) {
				if(!r.contains(loc)) return false;
			}
			return true;
		}
		
		@SafeVarargs
		<R extends Region> IntersectRegion(final R... regions) {
			super(regions);
		}
		
		<R extends Region> IntersectRegion(final Iterable<R> regions) {
			super(regions);
		}
		
	}
	
	private static final class UnionRegion<R extends Region> extends CompoundRegion<R> {
		
		@Override
		public final boolean contains(final Locatable loc) {
			for(final Region r : subRegions) {
				if(r.contains(loc)) return true;
			}
			return false;
		}
		
		@SafeVarargs
		UnionRegion(final R... regions) {
			super(regions);
		}
		
		UnionRegion(final Iterable<R> regions) {
			super(regions);
		}
		
	}
	
	private static final class PollableUnionRegion<R extends PollableRegion> extends CompoundRegion<R> implements PollableRegion {
		
		@Override
		public final boolean contains(final Locatable loc) {
			for(final PollableRegion r : subRegions) {
				if(r.contains(loc)) return true;
			}
			return false;
		}
		
		@Override
		public final Set<Location> getPoints() {
			final Set<Location> result = new HashSet<Location>();
			for(final PollableRegion r : subRegions) {
				result.addAll(r.getPoints());
			}
			return result;
		}
		
		@SafeVarargs
		PollableUnionRegion(final R... regions) {
			super(regions);
		}
		
		PollableUnionRegion(final Iterable<R> regions) {
			super(regions);
		}
		
	}
	
	private static final class DifferenceRegion extends CompoundRegion<Region> {
		
		private final Region initial;
		
		@Override
		public boolean contains(final Locatable loc) {
			if(!initial.contains(loc)) return false;
			for(final Region r : subRegions) {
				if(r.contains(loc)) return false;
			}
			return true;
		}
		
		DifferenceRegion(final Region initial, final Region... regions) {
			super(regions);
			this.initial = initial;
		}
		
		DifferenceRegion(final Region initial, final Iterable<? extends Region> regions) {
			super(regions);
			this.initial = initial;
		}
		
	}
	
	private static final class PollableComplementRegion<R extends PollableRegion> extends CompoundRegion<R> implements PollableRegion {
		
		private final Set<Location> points;
		
		@Override
		public Set<Location> getPoints() {
			return points;
		}

		public boolean contains(final Locatable loc) {
			return points.contains(loc);
		}
		
		@SafeVarargs
		public PollableComplementRegion(final R initial, final R... removed) {
			super(removed);
			final Set<Location> tempPoints = new HashSet<Location>(), newPoints = new HashSet<Location>();
			tempPoints.addAll(initial.getPoints());
			for(Region r : subRegions) {
				PollableRegion pr = (PollableRegion) r;
				tempPoints.addAll(pr.getPoints());
			}
			tempPoints.parallelStream().filter(new Predicate<Location>() {
				@Override
				public boolean test(final Location loc) {
					for(final PollableRegion r : subRegions) {
						if(r.contains(loc)) return false;
					}
					return true;
				}
			}).sequential().collect(Collectors.toCollection(new Supplier<Set<Location>>() {
				@Override
				public Set<Location> get() {
					return newPoints;
				}
			}));
			points = Collections.unmodifiableSet(newPoints);
		}

		public PollableComplementRegion(final R initial, final Iterable<R> removed) {
			super(removed);
			Set<Location> newPoints = new HashSet<Location>();
			newPoints.addAll(initial.getPoints());
			for(final Region r : subRegions) {
				PollableRegion pr = (PollableRegion) r;
				newPoints.addAll(pr.getPoints());
			}
			final Stream<Location> pointStream = newPoints.parallelStream().filter(new Predicate<Location>() {
				@Override
				public boolean test(final Location loc) {
					for(final PollableRegion r : subRegions) {
						if(r.contains(loc)) return false;
					}
					return true;
				}
			}).sequential();
			newPoints.clear();
			pointStream.collect(Collectors.toCollection(new Supplier<Set<Location>>() {
				@Override
				public Set<Location> get() {
					return newPoints;
				}
			}));
			points = Collections.unmodifiableSet(newPoints);
		}
		
	}
	
	private static final class ComplementRegion extends CompoundRegion<Region> {
		
		public boolean contains(Locatable loc) {
			for(final Region r : subRegions) {
				if(r.contains(loc)) return false;
			}
			return true;
		}
		
		ComplementRegion(final Region... regions) {
			super(regions);
		}
		
		ComplementRegion(final Iterable<Region> regions) {
			super(regions);
		}
		
	}
	
	private abstract static class CompoundRegion<R extends Region> extends SimpleRegion {
		
		protected final Set<R> subRegions = new HashSet<R>();
		
		@SafeVarargs
		public CompoundRegion(final R... regions) {
			for(final R r : regions) {
				subRegions.add((R) r);
			}
		}
		
		protected CompoundRegion(final Iterable<? extends R> regions) {
			for(final R r : regions) {
				subRegions.add((R) r);
			}
		}
		
	}
	
	
}
