package com.podts.rpg.server.model.universe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class SetTileSelection extends TileSelection {
	
	private final Set<Tile> tiles;
	private final Set<Tile> safeTiles;
	
	@Override
	public SelectionType getSelectionType() {
		return SelectionType.SET;
	}
	
	public Iterator<Tile> iterator() {
		return safeTiles.iterator();
	}
	
	@Override
	public int size() {
		return safeTiles.size();
	}
	
	public SetTileSelection(Tile... tiles) {
		this.tiles = new HashSet<Tile>();
		for(Tile tile : tiles) {
			this.tiles.add(tile);
		}
		safeTiles = Collections.unmodifiableSet(this.tiles);
	}
	
}
