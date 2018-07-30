package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.MovableFacable;

public interface Walkable extends MovableFacable {
	
	public Walkable walk(Direction direction);
	
}
