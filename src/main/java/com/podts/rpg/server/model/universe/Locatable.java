package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Locatable extends HasPlane {
	
	public Collection<Location> getLocations();
	
	public default Stream<Location> locations() {
		return getLocations().stream();
	}
	
	public default Location anyLocation() {
		return locations()
				.findAny()
				.orElse(Space.getNowhere());
	}
	
	public default boolean occupies(HasLocation loc) {
		return locations()
				.anyMatch(loc::isAt);
	}
	
	public default boolean isNowhere() {
		return getLocations().isEmpty();
	}
	
	public default boolean isSomewhere() {
		return !isNowhere();
	}
	
	public default long getArea() {
		return getLocations().size();
	}
	
	@Override
	public default Plane getPlane() {
		return locations()
				.findAny()
				.orElse(Space.getNowhere())
				.getPlane();
	}
	
	public default Stream<Tile> tiles() {
		return locations()
				.map(Location::getTile);
	}
	
	public default Collection<Tile> getTiles() {
		return tiles()
				.collect(Collectors.toSet());
	}
	
	public default double distance(Locatable other, double range) {
		range = Double.max(0d, range);
		Collection<? extends Location> points = getLocations();
		Collection<? extends Location> otherPoints = other.getLocations();
		double shortest = Double.MAX_VALUE;
		for(Location p : points) {
			for(Location o : otherPoints) {
				double length = p.distance(o);
				if(length <= range) return length;
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
	
	public default boolean isAt(Locatable point) {
		return locations()
				.anyMatch(point::isAt);
	}
	
	public default boolean isInPlane(int z) {
		return getPlane().getZ() == z;
	}
	
	public default boolean isInPlane(Plane plane) {
		return getPlane().equals(plane);
	}
	
	public default boolean isInPlane(Locatable l) {
		return isInPlane(l.getPlane());
	}
	
	public default boolean isBetweenPlanes(int minZ, int maxZ) {
		if(minZ > maxZ) {
			int tempZ = minZ;
			minZ = maxZ;
			maxZ = tempZ;
		}
		int z = getPlane().getZ();
		return z >= minZ &&
				z <= maxZ;
	}
	
	public default boolean isBetweenPlanes(Plane a, Plane b) {
		if(a.isInDifferentSpace(b)) return false;
		return isBetweenPlanes(a.getZ(), b.getZ());
	}
	
}
