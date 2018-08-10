package com.podts.rpg.server;

public abstract class GameState {
	
	private final int id;
	private final String name;
	
	public final String getName() {
		return name;
	}
	
	public final int getID() {
		return id;
	}
	
	protected abstract void onEnter(Player player, GameState previous);
	protected abstract void onLeave(Player player, GameState next);
	
	@Override
	public String toString() {
		return getName() + " State";
	}
	
	GameState(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
}
