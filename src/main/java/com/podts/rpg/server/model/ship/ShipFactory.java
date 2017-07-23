package com.podts.rpg.server.model.ship;

import com.podts.rpg.server.model.Galaxy;

public class ShipFactory {
	
	public static final Ship getStartingShip() {
		return new Ship(ShipType.ESCAPE_POD, Galaxy.getStartingLocation());
	}
	
}
