package com.podts.rpg.server.model.universe;

public abstract class TileSelection implements Iterable<Tile> {
	
	public enum SelectionType {
		SQUARE(),
		SET();
	}
	
	public abstract SelectionType getSelectionType();
	public abstract int size();
	
	public final TileSelection merge(final TileSelection other) {
		//TODO implement me
		return null;
	}
	
	protected TileSelection() {
		
	}
	
}
