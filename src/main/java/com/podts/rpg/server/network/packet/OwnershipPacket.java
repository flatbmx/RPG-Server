package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.model.Entity;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.network.Packet;

public class OwnershipPacket extends Packet {
	
	private final Player owner;
	private final Entity entity;
	
	public final Player getOwner() {
		return owner;
	}
	
	public final Entity getEntity() {
		return entity;
	}
	
	public OwnershipPacket(Player o, Entity e) {
		owner = o;
		entity = e;
	}
	
}
