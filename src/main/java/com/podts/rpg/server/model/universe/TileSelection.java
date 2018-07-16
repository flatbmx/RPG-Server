package com.podts.rpg.server.model.universe;

import java.util.stream.Stream;

public abstract class TileSelection implements Iterable<Tile> {
	
	public enum SelectionType {
		SQUARE(),
		SET();
	}
	
	public abstract SelectionType getSelectionType();
	public abstract int size();
	
	public abstract Stream<Tile> tiles();
	
	public Stream<Location> points() {
		return tiles()
				.map(Locatable::getLocation);
	}
	
	public boolean contains(Locatable point) {
		return tiles()
				.anyMatch(point::isAt);
	}
	
	public final TileSelection merge(final TileSelection other) {
		//TODO implement me
		return null;
	}
	
	protected TileSelection() {
		
	}
	
}
