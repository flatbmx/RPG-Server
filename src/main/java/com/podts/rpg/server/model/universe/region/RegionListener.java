package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.Location.MoveType;
import com.podts.rpg.server.model.entity.Entity;

public interface RegionListener {
	
	public void onEnter(Entity e, MoveType moveType);
	public void onLeave(Entity e, MoveType moveType);
	
}
