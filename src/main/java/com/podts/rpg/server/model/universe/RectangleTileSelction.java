package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class RectangleTileSelction extends TileSelection {
	
	private final Tile[][] tiles;
	
	@Override
	public SelectionType getSelectionType() {
		return SelectionType.RECTANGULAR;
	}
	
	@Override
	public Iterator<Tile> iterator() {
		return new DualArrayIterator();
	}
	
	public int getWidth() {
		return tiles.length;
	}
	
	public int getHeight() {
		return tiles[0].length;
	}
	
	@Override
	public int size() {
		return getWidth() * getHeight();
	}
	
	public Location getTopLeft() {
		return tiles[0][0].getLocation();
	}
	
	@Override
	public Stream<Tile> tiles() {
		return Arrays.stream(tiles)
				.flatMap(Arrays::stream);
	}
	
	public RectangleTileSelction(Tile[][] tiles) {
		this.tiles = tiles;
	}
	
	private final class DualArrayIterator implements Iterator<Tile> {
		
		private int i = 0, j = 0;
		
		@Override
		public boolean hasNext() {
			if(i < tiles.length) return true;
			if(j < tiles[i].length) return true;
			return false;
		}

		@Override
		public Tile next() {
			if(!hasNext()) throw new NoSuchElementException();
			if(i == tiles.length) {
				i = 0;
				++j;
			}
			return tiles[i++][j];
		}
		
	}
	
}
