package com.podts.rpg.server.model.universe.path;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Tile;

public abstract class Path implements Iterable<Tile> {
	
	
	
	static final boolean isValid(Path path) {
		if(path.length() == 1) return true;
		Iterator<Tile> it = path.getTiles().iterator();
		Tile recent = it.next();
		while(it.hasNext()) {
			Tile next = it.next();
			if(!recent.isInWalkingRange(next, 1)) return false;
			recent = next;
		}
		return true;
	}
	
	public abstract List<Tile> getTiles();
	
	public Stream<Tile> tiles() {
		return getTiles().stream();
	}
	
	@Override
	public Iterator<Tile> iterator() {
		return getTiles().iterator();
	}
	
	public boolean contains(Tile tile) {
		return tiles()
				.anyMatch(tile::isAt);
	}
	
	public int length() {
		return getTiles().size();
	}
	
	public Tile getStart() {
		return getTiles().get(0);
	}
	
	public Tile getEnd() {
		return getTiles().get(length() - 1);
	}
	
}
