package com.podts.rpg.server.network.packet;

import java.security.PublicKey;

import javax.crypto.SecretKey;

import com.podts.rpg.server.network.Packet;

public class AESReplyPacket extends Packet {
	
	/**
	 * The RSA public key to encrypt the AES key with.
	 */
	private final PublicKey publicKey;
	/**
	 * The AES key that will be used for this stream.
	 */
	private final SecretKey secret;
	
	public final PublicKey getPublicKey() {
		return publicKey;
	}
	
	public final SecretKey getSecret() {
		return secret;
	}
	
	public AESReplyPacket(PublicKey publicKey, SecretKey secret) {
		this.publicKey = publicKey;
		this.secret = secret;
	}
	
}
