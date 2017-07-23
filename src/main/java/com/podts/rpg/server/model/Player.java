package com.podts.rpg.server.model;

import com.podts.rpg.server.model.ship.Ship;
import com.podts.rpg.server.network.Stream;

public class Player implements Locatable {
	
	private static int currentID = 0;
	
	private static int getNewID() {
		return currentID++;
	}
	
	private static final Player[] players;
	private static final int MAX_PLAYERS = 100;
	
	static {
		players = new Player[MAX_PLAYERS];
	}
	
	public static final Player getPlayer(int id) {
		if(id >= 0 && id < MAX_PLAYERS) return players[id];
		return null;
	}
	
	private final int id;
	private Stream stream;
	private Ship ship;
	
	public final int getID() {
		return id;
	}
	
	public final Stream getStream() {
		return stream;
	}
	
	public final Ship getShip() {
		return ship;
	}

	public final void setShip(Ship ship) {
		this.ship = ship;
	}
	
	@Override
	public Location getLocation() {
		if(ship == null) return null;
		return ship.getLocation();
	}
	
	public Player(Stream stream) {
		id = getNewID();
		this.stream = stream;
	}
	
	public Player(Stream stream, Ship ship) {
		id = getNewID();
		this.stream = stream;
		this.ship = ship;
	}
	
}
