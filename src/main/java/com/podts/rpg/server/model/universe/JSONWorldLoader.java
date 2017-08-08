package com.podts.rpg.server.model.universe;

import java.io.File;

import com.google.gson.Gson;

public final class JSONWorldLoader extends WorldLoader {
	
	private static final Gson gson = new Gson();
	
	private final File root;
	
	@Override
	public void loadTiles(Tile[][] tiles, Location point) {
		// TODO Auto-generated method stub

	}

	@Override
	public Tile loadTile(Location point) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorldLoader saveTiles(Tile[][] tiles) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doSaveTile(Tile tile) {
		// TODO Auto-generated method stub

	}
	
	public JSONWorldLoader(File root) {
		this.root = root;
	}
	
}
