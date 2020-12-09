package com.podts.rpg.server.model.universe.structure;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Building extends Structure {
	
	public abstract Collection<Room> getRooms();
	
	public Stream<Room> rooms() {
		return getRooms().stream();
	}
	
	public Collection<Door> getDoors() {
		return doors()
				.collect(Collectors.toList());
	}
	
	public Stream<Door> doors() {
		return rooms()
		.flatMap(Room::doors)
		.distinct();
	}
	
}
