package com.podts.rpg.server.model.universe;

public class Tile implements Locatable {
	
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
	
	public final TileType getType() {
		return type;
	}
	
	public final Location getLocation() {
		return location;
	}
	
	public final boolean isTraversable() {
		return type.isTraversable();
	}
	
	protected Tile(TileType type, Location location) {
		this.type = type;
		this.location = location;
	}
	
}
