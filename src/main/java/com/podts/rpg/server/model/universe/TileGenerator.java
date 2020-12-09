package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.model.universe.TileElement.TileType;

public abstract class TileGenerator {
	
	public static void emptyRectangle(Tile topLeft, TileType walls, int thickness, int width, int length) {
		width += thickness;
		length += thickness;
		for(int i=0; i<thickness; ++i) {
			emptyRectangle(topLeft, walls, width--, length--);
			topLeft = topLeft.shift(1, 1);
		}
	}
	
	public static void emptyRectangle(Tile topLeft, TileType walls, int width, int length) {
		//Left -> Right
		for(int y=0; y<=length; y += length) {
			for(int x=0; x<width; ++x) {
				setElement(topLeft.shift(x, y), walls);
			}
		}
		//Up -> Down
		for(int x=0; x<=width; x += width) {
			for(int y=1; y<length-1; ++y) {
				setElement(topLeft.shift(x, y), walls);
			}
		}
	}
	
	public static void fillRectangle(Tile topLeft, TileType fill, int width, int length) {
		Tile left = topLeft;
		Tile current = topLeft;
		for(int y=0; y<length; ++y) {
			for(int x=0; x<width; ++x) {
				setElement(current, fill);
				current = current.shift(1, 0);
			}
			left = left.shift(0, 1);
			current = left;
		}
	}
	
	public static void fillSquare(Tile topLeft, TileType fill, int size) {
		fillRectangle(topLeft, fill, size, size);
	}
	
	protected static void setElement(Tile tile, TileType type) {
		tile.element = constructElement(type);
	}
	
	protected static TileElement constructElement(TileType type) {
		return new TileElement(type);
	}
	
	protected static final void setElement(Tile tile, TileElement element) {
		tile.element = element;
	}
	
}
