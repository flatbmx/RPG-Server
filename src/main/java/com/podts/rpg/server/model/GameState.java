package com.podts.rpg.server.model;

public enum GameState {
	
	LOGIN(0),
	PLAYING(1);
	
	private final int id;
	
	public final int getID() {
		return id;
	}
	
	private GameState(int id) {
		this.id = id;
	}
	
}
