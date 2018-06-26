package com.podts.rpg.server.model;

import com.podts.rpg.server.Player;

public interface PlayerLoginListener {
	
	public default void onPlayerLogin(Player player) {}
	public default void onPlayerLogout(Player player, Player.LogoutReason reason) {}
	
}
