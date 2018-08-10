package com.podts.rpg.server;

import com.podts.rpg.server.Player.LogoutReason;

public interface PlayerLoginListener {
	
	public default void onLogin(Player player) {}
	public default void onLogout(Player player, LogoutReason reason) {}
	
}
