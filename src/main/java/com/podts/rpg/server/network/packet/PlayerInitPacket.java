package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.network.Packet;

public final class PlayerInitPacket extends Packet {
	
	private final Player player;
	
	public Player getPlayer() {
		return player;
	}
	
	public PlayerInitPacket(Player player) {
		this.player = player;
	}
	
}
