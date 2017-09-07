package com.podts.rpg.server;

import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.Stream;

public class Player {
	
	private final int id;
	private String username, password;
	private PlayerEntity entity;
	private Stream stream;
	
	public final int getID() {
		return id;
	}
	
	public final Stream getStream() {
		return stream;
	}
	
	public final void setStream(Stream s) {
		stream = s;
	}
	
	Player(int id, Stream stream) {
		this.id = id;
		this.stream = stream;
	}
	
	Player(int id, String username, String hashPassword) {
		this.id = id;
		this.username = username;
		this.password = hashPassword;
	}
	
	public final String getUsername() {
		return username;
	}
	
	public final String getPassword() {
		return password;
	}
	
	public PlayerEntity getEntity() {
		return entity;
	}
	
	//TODO Really need to change this scope.
	public void setEntity(PlayerEntity e) {
		entity = e;
	}

	public final void sendPacket(Packet... packets) {
		for(Packet p : packets)
			getStream().sendPacket(p);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Player) {
			Player other = (Player) o;
			return id == other.id;
		}
		return false;
	}
	
	public static enum LogoutReason {
		DISCONNECT(),
		LOGOUT();
	}
	
}
