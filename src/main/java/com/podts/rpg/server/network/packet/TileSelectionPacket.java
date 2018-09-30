package com.podts.rpg.server.network.packet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.network.Packet;

public final class TileSelectionPacket extends Packet {
	
	private final Collection<Tile> tiles;
	
	public Collection<Tile> getSelections() {
		return tiles;
	}
	
	public TileSelectionPacket(Collection<Tile> tiles) {
		this.tiles = Collections.unmodifiableCollection(tiles);
	}
	
	public TileSelectionPacket(Tile... tiles) {
		final Set<Tile> sels = new HashSet<>();
		for(Tile t : tiles)
			sels.add(t);
		this.tiles = Collections.unmodifiableSet(sels);
	}
	
}
