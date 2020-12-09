package com.podts.rpg.server.network.packet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.network.Packet;

public final class TileSelectionPacket extends Packet {
	
	public enum SelectionType {
		ADD(),
		REMOVE(),
		TOTAL();
	}
	
	private final Collection<Tile> tiles;
	private final SelectionType type;
	
	public Collection<Tile> getSelections() {
		return tiles;
	}
	
	public final SelectionType getType() {
		return type;
	}
	
	public TileSelectionPacket(SelectionType type, Collection<Tile> tiles) {
		this.tiles = Collections.unmodifiableCollection(tiles);
		this.type = type;
	}
	
	public TileSelectionPacket(SelectionType type, Tile... tiles) {
		this.type = type;
		final Set<Tile> sels = new HashSet<>();
		for(Tile t : tiles)
			sels.add(t);
		this.tiles = Collections.unmodifiableSet(sels);
	}
	
}
