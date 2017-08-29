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
	
	protected PlayerEntity(Player player, Location loc) {
		super(EntityType.PLAYER, player.getUsername(), loc);
		this.player = player;
	}
	
}
