package com.podts.rpg.server.model.universe;

public class TileSpatial implements HasLocation {
	
	private Tile tile;
	
	public boolean hasTile() {
		return isSomewhere();
	}
	
	@Override
	public final Tile getTile() {
		return tile;
	}
	
	@Override
	public final Location getLocation() {
		return getTile().getLocation();
	}
	
	final TileSpatial setLocation(Tile tile) {
		this.tile = tile;
		return this;
	}
	
	TileSpatial(Tile tile) {
		this.tile = Tile.validate(tile);
	}
	
	TileSpatial(Location location) {
		this(Location.validate(location).getTile());
	}
	
	TileSpatial() {
		this.tile = Space.NOWHERE_TILE;
	}
	
}
