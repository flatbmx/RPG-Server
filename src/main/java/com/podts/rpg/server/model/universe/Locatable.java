package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Occupies a certian Plane that can be nowhere(no locations) or multiple locations that all occupy the same plane and are connected.
 * @author David
 *
 */
public interface Locatable extends HasPlane {
	
	public Collection<Location> getLocations();
	
	public default Stream<Location> locations() {
		return getLocations().stream();
	}
	
	/**
	 * Returns any location that this Locatable occupies.
	 * If this is nowhere it will return the nowhere Location.
	 * @return
	 */
	public default Location anyLocation() {
		return locations()
				.findAny()
				.orElse(Space.NOWHERE);
	}
	
	public default boolean occupies(HasLocation loc) {
		return getLocations().contains(loc.getLocation());
	}
	
	public default boolean isNowhere() {
		return getLocations().isEmpty();
	}
	
	public default boolean isSomewhere() {
		return !isNowhere();
	}
	
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
	
	public default int walkingDistance(Locatable other, int range) {
		range = Integer.max(0, range);
		Collection<? extends Location> points = getLocations();
		Collection<? extends Location> otherPoints = other.getLocations();
		int shortest = Integer.MAX_VALUE;
		for(Location p : points) {
			for(Location o : otherPoints) {
				int length = p.walkingDistance(o);
				if(length <= range) return length;
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
