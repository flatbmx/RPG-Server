package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.region.IncompleteRegion;

public abstract class Plane extends IncompleteRegion implements HasSpace {
	
	private final int z;
	
	public final int getZ() {
		return z;
	}
	
	public abstract Collection<Tile> getTiles();
	
	public Stream<Tile> tiles() {
		return getTiles().stream();
	}
	
	public Tile getTile(Location point) {
		if(!contains(point)) return null;
		return tiles()
				.filter(t -> t.isAt(point))
				.findAny()
				.orElse(null);
	}
	
	public final Location createLocation(int x, int y) {
		return getSpace().createLocation(x, y, getZ());
	}
	
	@Override
	public final boolean contains(Locatable l) {
		return getZ() == l.getLocation().getZ() &&
				getSpace().equals(l.getSpace());
	}
	
	Plane(int z) {
		this.z = z;
	}
	
}
