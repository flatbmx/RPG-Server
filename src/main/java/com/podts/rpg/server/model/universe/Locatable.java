package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Occupies a certain Plane that can be nowhere(no locations) or multiple connected locations.
 * @author David
 *
 */
public interface Locatable extends HasPlane {
	
	/**
	 * Returns a collection that contains all the points that this occupies.
	 * All points in the returned collection are in the same {@link Plane} and are connected.
	 * If this object is nowhere then the returned collection will be empty.
	 * Due to how often this method may be called by other methods it is highly recommended that the 
	 * returned collection be cached/stored and not generated on demand otherwise performance may be lost.
	 * @return Collection of all points this occupies
	 */
	public Collection<Location> getLocations();
	
	/**
	 * Returns a stream consisting of all occupied points.
	 * Default implementation returns {@link #getLocations()}.{@link Collection#stream() stream()}
	 * @return Stream consisting of all occupied points
	 */
	public default Stream<Location> locations() {
		return getLocations().stream();
	}
	
	/**
	 * Returns any location that this Locatable occupies.
	 * If this occupies no locations it will return {@link Space#NOWHERE}.
	 * @return any Location this occupies or {@link Space#NOWHERE}.
	 */
	public default Location anyLocation() {
		return locations()
				.findAny()
				.orElse(Space.NOWHERE);
	}
	
	public default boolean occupies(HasLocation loc) {
		return getLocations().contains(loc.getLocation());
	}
	
	@Override
	public default boolean isNowhere() {
		return getLocations().isEmpty();
	}
	
	/**
	 * Returns the number of tiles this occupies.
	 * If this is nowhere it will return 0.
	 * @return number of tiles this occupies.
	 */
	public default int getArea() {
		return getLocations().size();
	}
	
	@Override
	public default Space getSpace() {
		return anyLocation().getSpace();
	}
	
	@Override
	public default Plane getPlane() {
		return anyLocation().getPlane();
	}
	
	public default Stream<Tile> tiles() {
		return locations()
				.map(Location::getTile);
	}
	
	public default Collection<Tile> getTiles() {
		return tiles()
				.collect(Collectors.toSet());
	}
	
	public default double distance(Locatable other, double cutoff) {
		cutoff = Double.max(0d, cutoff);
		Collection<? extends Location> points = getLocations();
		Collection<? extends Location> otherPoints = other.getLocations();
		double shortest = Double.MAX_VALUE;
		for(Location p : points) {
			for(Location o : otherPoints) {
				double length = p.distance(o);
				if(length <= cutoff) return length;
				if(length < shortest)
					shortest = length;
			}
		}
		return shortest;
	}
	
	public default double distance(Locatable other) {
		return distance(other, 0);
	}
	
	public default int walkingDistance(Locatable other, int cutoff) {
		cutoff = Integer.max(0, cutoff);
		Collection<? extends Location> points = getLocations();
		Collection<? extends Location> otherPoints = other.getLocations();
		int shortest = Integer.MAX_VALUE;
		for(Location p : points) {
			for(Location o : otherPoints) {
				int length = p.walkingDistance(o);
				if(length <= cutoff) return length;
				if(length < shortest)
					shortest = length;
			}
		}
		return shortest;
	}
	
	public default int walkingDistance(Locatable other) {
		return walkingDistance(other, 0);
	}
	
	public default boolean isInRange(Locatable o, double distance) {
		return distance(o, distance) <= distance;
	}
	
	public default boolean isInWalkingRange(Locatable o, int distance) {
		return walkingDistance(o, distance) <= distance;
	}
	
	public default boolean isAt(Locatable loc) {
		return locations()
				.anyMatch(loc::occupies);
	}
	
}
