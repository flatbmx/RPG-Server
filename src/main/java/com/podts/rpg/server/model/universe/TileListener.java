package com.podts.rpg.server.model.universe;

public interface TileListener {
	
	public default void onAdd(Tile tile) {
		
	}
	
	public default void onRemove(Tile tile) {
		
	}
	
	public default boolean onChange(Tile tile, TileElement newElement) {
		return false;
	}
	
	public default boolean onUpdate(Tile tile) {
		return false;
	}
	
}
