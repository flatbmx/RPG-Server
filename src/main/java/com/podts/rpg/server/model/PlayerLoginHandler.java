package com.podts.rpg.server.model;

public interface PlayerLoginHandler {
	
	public default void onPlayerLogin(Player player) {}
	public default void onPlayerLogout(Player player) {}
	
}
