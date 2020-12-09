package com.podts.rpg.server.model.universe.structure;

import java.util.Collection;
import java.util.stream.Stream;

public abstract class Door {
	
	public abstract Collection<? extends Room> getRooms();
	
	public Stream<? extends Room> rooms() {
		return getRooms().stream();
	}
	
}
