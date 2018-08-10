package com.podts.rpg.server.model.universe;

import com.podts.rpg.server.Utils;

/**
 * A class that can generate Tiles based on a location.
 *
 */
public abstract class WorldGenerator {
	
	public final Tile generateTile(Location point) {
		Utils.assertNull(point, "Cannot generate Tile with null location.");
		return doGenerateTile(point);
	}
	
	protected abstract Tile doGenerateTile(Location point);
	
	public final Tile[][] generateRectTiles(Location point, int width, int height) {
		Utils.assertNull(point, "Cannot generate Tiles with null starting point.");
		Utils.assertArg(width <= 0 || height <= 0, "Cannot generate Tiles with negative or 0 width/height.");
		Tile[][] result = new Tile[width][height];
		doGenerateRectTiles(result, point);
		return result;
	}
	
	/**
	 * Generate tiles in a rectangular region and save them in the given array.
	 * The size of the rectangular region is determined by the size of the given array.
	 * @param tiles - The array to store the generated Tiles.
	 * @param x - The top left X position of the rectangle.
	 * @param y - The top left y position of the rectangle.
	 * @param z - The z position of the tiles.
	 * @return The WorldGenerator for chaining.
	 */
	public final WorldGenerator generateRectTiles(Tile[][] tiles, Location point) {
		Utils.assertNull(tiles, "Cannot generate Tiles with a null array.");
		Utils.assertNull(point, "Cannot generate Tiles with a null starting location.");
		Utils.assertArg(tiles.length == 0 || tiles[0].length == 0, "Cannot generate Tiles with an array of size 0.");
		
		return doGenerateRectTiles(tiles, point);
	}
	
	protected WorldGenerator doGenerateRectTiles(Tile[][] tiles, Location point) {
		for(int dy=0; dy < tiles[0].length; ++dy) {
			for(int dx=0; dx < tiles.length; ++dx) {
				tiles[dx][dy] = doGenerateTile(point.shift(dx, dy));
			}
		}
		
		return this;
	}
	
	public WorldGenerator() {
		
	}
	
}
