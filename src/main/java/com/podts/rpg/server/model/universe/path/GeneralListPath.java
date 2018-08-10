package com.podts.rpg.server.model.universe.path;

import java.util.Arrays;
import java.util.List;

import com.podts.rpg.server.model.universe.Tile;

class GeneralListPath extends Path implements ListPath {
	
	private final List<Tile> list;
	
	@Override
	public List<Tile> getTiles() {
		return list;
	}
	
	GeneralListPath(List<Tile> list) {
		this.list = list;
	}
	
	GeneralListPath(Tile... tiles) {
		list = Arrays.asList(tiles);
	}
	
}
