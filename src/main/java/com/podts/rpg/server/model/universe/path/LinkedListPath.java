package com.podts.rpg.server.model.universe.path;

import java.util.LinkedList;
import java.util.List;

import com.podts.rpg.server.model.universe.Tile;

class LinkedListPath extends Path implements ListPath {
	
	private final LinkedList<Tile> list;
	
	@Override
	public List<Tile> getTiles() {
		return list;
	}
	
	public Tile getFirst() {
		return list.getFirst();
	}
	
	@Override
	public Tile getEnd() {
		return list.getLast();
	}
	
	LinkedListPath(LinkedList<Tile> list) {
		this.list = list;
	}

	@Override
	public int getTurns() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
