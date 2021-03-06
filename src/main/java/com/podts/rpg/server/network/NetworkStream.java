package com.podts.rpg.server.network;

import java.net.InetAddress;

import javax.crypto.SecretKey;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.network.packet.AcknowledgePacket;
import com.podts.rpg.server.network.packet.AcknowledgementPacket;

public interface NetworkStream {
	
	public boolean isOpen();
	
	public void closeStream();
	
	public InetAddress getAddress();
	
	public int getPing();
	
	public Player getPlayer();
	
	void setPlayer(Player player);
	
	public SecretKey getSecretKey();
	
	public void sendPacket(Packet p);
	
	public void sendPacket(Packet... packets);
	
	public int getFlagTolerance();
	public int getFlags();
	public NetworkStream flag(int severity);
	
	public default NetworkStream flag() {
		return flag(1);
	}
	
	public default void acknowledge(AcknowledgementPacket packet) {
		sendPacket(new AcknowledgePacket(packet));
	}
	
	public default String ownerString() {
		if(getPlayer() != null) {
			return getPlayer().getName();
		}
		return getAddress().toString();
	}
	
}
