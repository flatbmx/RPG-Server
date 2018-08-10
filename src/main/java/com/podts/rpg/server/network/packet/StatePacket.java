package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.GameState;
import com.podts.rpg.server.network.Packet;

public final class StatePacket extends Packet {
	
	private final GameState state;
	
	public GameState getState() {
		return state;
	}
	
	public StatePacket(GameState newState) {
		state = newState;
	}
	
}
