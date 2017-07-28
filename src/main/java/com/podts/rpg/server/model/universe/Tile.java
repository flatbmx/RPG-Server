package com.podts.rpg.server.model.universe;

public final class Tile {
	
	public enum TileType {
		VOID(false),
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
	
	private final TileType type;
	
	public TileType getType() {
		return type;
	}
	
	public boolean isTraversable() {
		return type.isTraversable();
	}
	
	public Tile(TileType type) {
		this.type = type;
	}
	
}
