package com.podts.rpg.server.model;

import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;

public class PlayerEntity extends Entity {
	
	private final Player player;
	
	public final Player getPlayer() {
		return player;
	}
	
	//TODO Make this protected!
	public PlayerEntity(Player player, Location loc) {
		super(EntityType.PLAYER, loc);
		this.player = player;
	}
	
}
