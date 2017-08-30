package com.podts.rpg.server.model.universe;

/**
 * A class that can generate Tiles based on a location.
 *
 */
public abstract class WorldGenerator {
	
	public final Tile generateTile(Location point) {
		if(point == null) throw new IllegalArgumentException("Cannot generate Tile with null location.");
		return doGenerateTile(point);
	}
	
	protected abstract Tile doGenerateTile(Location point);
	
	public final Tile[][] generateRectTiles(Location point, int width, int height) {
		if(point == null) throw new IllegalArgumentException("Cannot generate Tiles with null starting point.");
		if(width <= 0 || height <= 0) throw new IllegalArgumentException("Cannot generate Tiles with negative or 0 width/height.");
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
		if(tiles == null) throw new IllegalArgumentException("Cannot generate Tiles with a null array.");
		if(point == null) throw new IllegalArgumentException("Cannot generate Tiles with a null starting location.");
		if(tiles.length == 0 || tiles[0].length == 0) throw new IllegalArgumentException("Cannot generate Tiles with an array of size 0.");
		
		return doGenerateRectTiles(tiles, point);
	}
	
	protected WorldGenerator doGenerateRectTiles(Tile[][] tiles, Location point) {
		
		for(int dy=0; dy < tiles[0].length; ++dy) {
			for(int dx=0; dx < tiles.length; ++dx) {
				tiles[dx][dy] = generateTile(point.move(dx, dy, 0));
			}
		}
		
		return this;
	}
	
	public WorldGenerator() {
		
	}
	
}
