package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;

public class EntityPacket extends AcknowledgementPacket {
	
	public static final EntityPacket constructCreate(Entity entity) {
		return new EntityPacket(entity, UpdateType.CREATE);
	}
	
	public static final EntityPacket constructUpdate(Entity entity) {
		return new EntityPacket(entity, UpdateType.UPDATE, entity.getLocation());
	}
	
	public static final EntityPacket constructDestroy(Entity entity) {
		return new EntityPacket(entity, UpdateType.DESTROY);
	}
	
	public static final EntityPacket constructMove(Entity entity, Location newLocation) {
		return new EntityPacket(entity, UpdateType.UPDATE, newLocation);
	}
	
	public static final EntityPacket constructMove(Entity entity, Location newLocation, int ack) {
		return new EntityPacket(entity, UpdateType.UPDATE, newLocation, ack);
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
	
	private EntityPacket(Entity entity, UpdateType type, Location newLocation, int ack) {
		super(ack);
		this.type = type;
		this.entity = entity;
		this.newLocation = newLocation;
	}
	
	private EntityPacket(Entity entity, UpdateType type) {
		this(entity, type, entity.getLocation(), -1);
	}
	
	private EntityPacket(Entity entity, UpdateType type, Location newLocation) {
		this(entity, type, newLocation, -1);
	}
	
}
