package com.podts.rpg.server.model.ship;

import com.podts.rpg.server.model.Entity;
import com.podts.rpg.server.model.Location;

public class Ship extends Entity {
	
	private final ShipType type;
	
	public final ShipType getShipType() {
		return type;
	}
	
	public Ship(ShipType shipType, Location loc) {
		super(shipType.getEntityType(), loc);
		type = shipType;
	}

}
