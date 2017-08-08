package com.podts.rpg.server.model.universe;

public abstract class WorldLoader {
	
	public final Tile[][] loadTiles(Location point, int width, int height) {
		Tile[][] result = new Tile[width][height];
		loadTiles(result, point);
		return result;
	}
	
	public abstract void loadTiles(Tile[][] tiles, Location point);
	public abstract Tile loadTile(Location point);
	
	public abstract WorldLoader saveTiles(Tile[][] tiles);
	
	public WorldLoader saveTile(Tile tile) {
		doSaveTile(tile);
		return this;
	}
	
	protected abstract void doSaveTile(Tile tile);
	
}
