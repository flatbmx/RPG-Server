package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.Stream;

public final class LoginPacket extends Packet {
	
	private final String username, password;
	
	public final String getUsername() {
		return username;
	}

	public final String getPassword() {
		return password;
	}

	public LoginPacket(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
}
