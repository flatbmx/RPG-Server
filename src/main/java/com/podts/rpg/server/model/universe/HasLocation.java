package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public interface HasLocation extends Locatable {
	
	public Location getLocation();
	
	public default Tile getTile() {
		return getLocation().getTile();
	}
	
	@Override
	public default boolean isNowhere() {
		return getLocation().isNowhere();
	}
	
	@Override
	public default Collection<Location> getLocations() {
		if(isNowhere())
			return Collections.emptySet();
		return Collections.singleton(getLocation());
	}
	
	@Override
	public default Stream<Location> locations() {
		if(isNowhere())
			return Stream.empty();
		return Stream.of(getLocation());
	}
	
	@Override
	public default Space getSpace() {
		return getLocation().getSpace();
	}
	
	@Override
	public default Plane getPlane() {
		return getLocation().getPlane();
	}
	
	public default boolean isAt(HasLocation loc) {
		return getLocation().equals(loc.getLocation());
	}
	
	@Override
	public default boolean isAt(Locatable loc) {
		return loc.locations()
				.anyMatch(getLocation()::isAt);
	}
	
	@Override
	public default int getArea() {
		return 1;
	}
	
	public default Stream<Tile> surroundingTiles(int radius) {
		return getSpace().surroundingTiles(this, radius);
	}
	
	public default Stream<Tile> surroundTiles() {
		return surroundingTiles(1);
	}
	
}