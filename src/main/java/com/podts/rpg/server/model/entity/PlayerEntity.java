package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;

public class PlayerEntity extends Entity {
	
	private final Player player;
	
	public final Player getPlayer() {
		return player;
	}
	
	public PlayerEntity(Player player, Location loc) {
		super(EntityType.PLAYER, loc);
		this.player = player;
	}
	
}
