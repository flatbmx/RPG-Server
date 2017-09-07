package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.universe.Location;

public class EntityFactory {
	
	public static final PlayerEntity constructPlayerEntity(final Player player, final Location location) {
		return new PlayerEntity(player, location);
	}
	
	private EntityFactory() {
		
	}
	
}
