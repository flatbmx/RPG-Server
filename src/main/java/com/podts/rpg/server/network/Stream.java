package com.podts.rpg.server.network;

import java.net.InetAddress;

import javax.crypto.SecretKey;

import com.podts.rpg.server.model.Player;

public interface Stream {
	
	public boolean isOpen();
	
	public void closeStream();
	
	public InetAddress getAddress();
	
	public Player getPlayer();
	
	public SecretKey getSecretKey();
	
	public void sendPacket(Packet p);
	
}
