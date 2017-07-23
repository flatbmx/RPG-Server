package com.podts.rpg.server.network.packet;

import java.security.PublicKey;

import com.podts.rpg.server.network.Packet;

public final class RSAHandShakePacket extends Packet {
	
	private final PublicKey publicKey;
	
	public final PublicKey getPublicKey() {
		return publicKey;
	}
	
	@Override
	public void handle() {
		
	}
	
	public RSAHandShakePacket(PublicKey pub) {
		publicKey = pub;
	}
	
}
