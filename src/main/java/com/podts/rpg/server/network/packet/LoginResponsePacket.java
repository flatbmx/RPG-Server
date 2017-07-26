package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.network.Packet;

public class LoginResponsePacket extends Packet {
	
	private final Location location;
	
	public LoginResponsePacket(final Location initialLocation) {
		location = initialLocation;
	}
	
}
