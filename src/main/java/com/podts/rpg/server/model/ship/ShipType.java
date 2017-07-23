package com.podts.rpg.server.model.ship;

import com.podts.rpg.server.model.EntityType;

public enum ShipType {
	
	ESCAPE_POD(EntityType.SHIP_ESCAPEPOD);
	
	private final EntityType type;
	
	public final EntityType getEntityType() {
		return type;
	}
	
	private ShipType(EntityType t) {
		type = t;
	}
	
}
