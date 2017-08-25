package com.podts.rpg.server.network.packet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.universe.TileSelection;
import com.podts.rpg.server.network.Packet;

public final class TileSelectionPacket extends Packet {
	
	private final Set<TileSelection> selections;
	
	public Set<TileSelection> getSelections() {
		return selections;
	}
	
	public TileSelectionPacket(TileSelection... selections) {
		final Set<TileSelection> sels = new HashSet<TileSelection>();
		for(TileSelection selection : selections) {
			sels.add(selection);
		}
		this.selections = Collections.unmodifiableSet(sels);
	}
	
}
