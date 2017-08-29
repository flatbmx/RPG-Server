package com.podts.rpg.server.model.universe;

public final class Tile implements Locatable {
	
	public enum TileType {
		VOID(false),
		GRASS(),
		DIRT(),
		SAND(),
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
	
	private final TileType type;
	private final Location location;
	
	public TileType getType() {
		return type;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public boolean isTraversable() {
		return type.isTraversable();
	}
	
	public Tile(TileType type, Location location) {
		this.type = type;
		this.location = location;
	}
	
}
