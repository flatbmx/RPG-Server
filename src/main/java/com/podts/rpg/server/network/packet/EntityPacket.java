package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.network.Packet;

public class EntityPacket extends Packet {
	
	public static final EntityPacket constructCreate(Entity entity) {
		return new EntityPacket(entity, UpdateType.CREATE);
	}
	
	public static final EntityPacket constructUpdate(Entity entity) {
		return new EntityPacket(entity, UpdateType.UPDATE, entity.getLocation());
	}
	
	public static final EntityPacket constructDestroy(Entity entity) {
		return new EntityPacket(entity, UpdateType.DESTROY);
	}
	
	public enum UpdateType {
		CREATE(),
		UPDATE(),
		DESTROY();
	}
	
	private final Entity entity;
	private final UpdateType type;
	private final Location newLocation;
	
	public final Entity getEntity() {
		return entity;
	}
	
	public final UpdateType getType() {
		return type;
	}
	
	public final Location getNewLocation() {
		return newLocation;
	}
	
	private EntityPacket(Entity entity, UpdateType type) {
		this.type = type;
		this.entity = entity;
		this.newLocation = entity.getLocation();
	}
	
	private EntityPacket(Entity entity, UpdateType type, Location newLocation) {
		this.type = type;
		this.entity = entity;
		this.newLocation = newLocation;
	}
	
}
