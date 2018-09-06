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
	public default Collection<Location> getLocations() {
		return Collections.singleton(getLocation());
	}
	
	@Override
	public default Stream<Location> locations() {
		return Stream.of(getLocation());
	}
	
	@Override
	public default long getArea() {
		return 1;
	}
	
	public default Stream<Tile> surroundingTiles(int radius) {
		return getSpace().surroundingTiles(this, radius);
	}
	
	public default Stream<Tile> surroundTiles() {
		return surroundingTiles(1);
	}
	
}
