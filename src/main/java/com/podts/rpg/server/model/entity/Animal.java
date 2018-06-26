package com.podts.rpg.server.model.entity;

public interface Animal extends Livable {
	
	public boolean isAwake();
	
	public default boolean isSleeping() {
		return !isDead() && !isAwake();
	}
	
}
