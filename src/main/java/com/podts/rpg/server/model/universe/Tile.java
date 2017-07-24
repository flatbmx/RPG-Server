package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.Location;

public final class Tile {
	
	public enum TileType {
		GRASS(),
		DIRT(),
		WATER(false);
		
		private final boolean traversable;
		
		public boolean isTraversable() {
			return traversable;
		}
		
		private TileType() {
			traversable = true;
		}
		
		private TileType(boolean travel) {
			traversable = travel;
		}
		
	}
	
	private TileType type;
	private final Location location;
	
	public final TileType getType() {
		return type;
	}
	
	public boolean isTraversable() {
		return type.isTraversable();
	}
	
	public Location getLocation() {
		return location;
	}
	
	protected Tile(Location location, TileType type) {
		this.location = location;
		this.type = type;
	}
	
}
