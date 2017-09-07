package com.podts.rpg.server.network;

import java.net.InetAddress;

import javax.crypto.SecretKey;

import com.podts.rpg.server.Player;

public interface Stream {
	
	public boolean isOpen();
	
	public void closeStream();
	
	public InetAddress getAddress();
	
	public Player getPlayer();
	
	public void setPlayer(Player player);
	
	public SecretKey getSecretKey();
	
	public void sendPacket(Packet p);
	
}
