package com.podts.rpg.server.model.universe.structure;

import java.util.Collection;
import java.util.stream.Stream;

public abstract class Room {
	
	public abstract Collection<Door> getDoors();
	
	public Stream<Door> doors() {
		return getDoors().stream();
	}
	
	public boolean connectsTo(Room room) {
		return doors()
			.flatMap(Door::rooms)
			.distinct()
			.anyMatch(room::equals);
	}
	
}
