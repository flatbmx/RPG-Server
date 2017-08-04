package com.podts.rpg.server.model;

import com.podts.rpg.server.network.Stream;

public class Player {
	
	private final int id;
	private PlayerEntity entity;
	private Stream stream;
	
	public final int getID() {
		return id;
	}
	
	public final Stream getStream() {
		return stream;
	}
	
	public final void setStream(Stream s) {
		stream = s;
	}
	
	public Player(Stream stream) {
		id = getNewID();
		this.stream = stream;
	}
	
	public Player() {
		id = getNewID();
	}

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
	
	public PlayerEntity getEntity() {
		return entity;
	}
	
	//TODO Really need to change this scope.
	public void setEntity(PlayerEntity e) {
		entity = e;
	}
	
}
