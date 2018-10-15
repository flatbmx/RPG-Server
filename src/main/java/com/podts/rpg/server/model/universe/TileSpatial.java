package com.podts.rpg.server.model.universe;

public class TileSpatial implements HasLocation {
	
	public static final Tile validate(Tile tile) {
		if(tile == null)
			return Space.NOWHERE_TILE;
		return tile;
	}
	
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
		this.tile = validate(tile);
	}
	
	TileSpatial(Location location) {
		this(Spatial.validate(location).getTile());
	}
	
	TileSpatial() {
		this.tile = Space.NOWHERE_TILE;
	}
	
}
