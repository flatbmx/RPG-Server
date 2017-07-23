package com.podts.rpg.server.model;

import com.podts.rpg.server.network.Stream;

public class Player {
	
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
	
	public final int getID() {
		return id;
	}
	
	public final Stream getStream() {
		return stream;
	}
	
	public Player(Stream stream) {
		id = getNewID();
		this.stream = stream;
	}
	
}
