package com.podts.rpg.server.model.entity;

public interface Livable {
	
	public boolean isAlive();
	
	public default boolean isDead() {
		return !isAlive();
	}
	
}
