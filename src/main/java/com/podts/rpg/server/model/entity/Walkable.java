package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Location.Direction;

public interface Walkable extends Locatable {
	
	public Walkable walk(Direction direction);
	
}
