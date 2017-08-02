package com.podts.rpg.server.model.universe;

/**
 * A class that can generate Tiles based on a location.
 *
 */
public abstract class WorldGenerator {
	
	public abstract Tile generateTile(Location point);
	
	public Tile[][] generateRectTiles(Location point, int width, int height) {
		Tile[][] result = new Tile[width][height];
		generateRectTiles(result,point);
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
	public WorldGenerator generateRectTiles(Tile[][] tiles, Location point) {
		
		for(int dy=0; dy < tiles[0].length; ++dy) {
			for(int dx=0; dx < tiles.length; ++dx) {
				tiles[dx][dy] = generateTile(point.move(dx, dy, 0));
			}
		}
		
		return this;
	}
	
}
