package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.model.Entity;
import com.podts.rpg.server.network.Packet;

public class EntityPacket extends Packet {
	
	public enum UpdateType {
		CREATE(),
		UPDATE(),
		DESTROY();
	}
	
	private final Entity entity;
	private final UpdateType type;
	
	public final Entity getEntity() {
		return entity;
	}
	
	public final UpdateType getType() {
		return type;
	}
	
	public EntityPacket(Entity entity, UpdateType type) {
		this.type = type;
		this.entity = entity;
	}
	
}
