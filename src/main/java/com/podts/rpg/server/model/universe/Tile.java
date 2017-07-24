package com.podts.rpg.server.model.universe;

public class Tile {
	
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
	
	public final TileType getType() {
		return type;
	}
	
	public Tile(TileType type) {
		this.type = type;
	}
	
}
