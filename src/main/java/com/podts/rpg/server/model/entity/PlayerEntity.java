package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Location;

public class PlayerEntity extends LivingHumanoid {
	
	public static int DEFAULT_VIEW_DISTANCE = 15;
	
	private final Player player;
	
	public final Player getPlayer() {
		return player;
	}
	
	PlayerEntity(Player player, Location loc) {
		super(player.getUsername(), EntityType.PLAYER, loc, DEFAULT_VIEW_DISTANCE);
		this.player = player;
	}
	
}
