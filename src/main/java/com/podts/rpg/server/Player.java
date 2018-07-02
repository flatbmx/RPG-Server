package com.podts.rpg.server;

import com.podts.rpg.server.command.CommandSender;
import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.MessagePacket;

public class Player implements CommandSender {
	
	public static final boolean is(Locatable l) {
		return l instanceof PlayerEntity;
	}
	
	public static final boolean is(Entity e) {
		if(e == null) return false;
		return EntityType.PLAYER.equals(e.getType());
	}
	
	public static final Player get(Entity e) {
		if(e instanceof PlayerEntity) {
			PlayerEntity pE = (PlayerEntity) e;
			return pE.getPlayer();
		}
		return null;
	}
	
	private final int id;
	private String username, password;
	private PlayerEntity entity;
	private NetworkStream networkStream;
	
	public final int getID() {
		return id;
	}
	
	@Override
	public final String getName() {
		return username;
	}
	
	public final NetworkStream getStream() {
		return networkStream;
	}
	
	//TODO REALLY REALLY need to change this scope.
	public final void setStream(NetworkStream s) {
		networkStream = s;
	}
	
	public final String getUsername() {
		return username;
	}
	
	public final String getPassword() {
		return password;
	}
	
	public final PlayerEntity getEntity() {
		return entity;
	}
	
	//TODO REALLY need to change this scope.
	public void setEntity(PlayerEntity e) {
		entity = e;
	}

	public final void sendPacket(Packet... packets) {
		for(Packet p : packets)
			getStream().sendPacket(p);
	}
	
	@Override
	public final void sendMessage(String message) {
		sendPacket(new MessagePacket(message));
	}
	
	@Override
	public final void sendMessage(CommandSender sender, String message) {
		sendPacket(new MessagePacket(sender, message));
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof Player) {
			Player other = (Player) o;
			return id == other.id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Player - " + username;
	}
	
	Player(int id, NetworkStream networkStream) {
		this.id = id;
		this.networkStream = networkStream;
	}
	
	Player(int id, String username, String hashPassword) {
		this.id = id;
		this.username = username;
		this.password = hashPassword;
	}
	
	public static enum LogoutReason {
		DISCONNECT(),
		LOGOUT();
	}
	
}
