package com.podts.rpg.server.model;

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
	
	public Player(Stream stream) {
		id = getNewID();
		this.stream = stream;
	}
	
	public Player() {
		id = getNewID();
	}
	
	public Player(String username, String hashPassword) {
		id = getNewID();
		this.username = username;
		this.password = hashPassword;
	}
	
	private static int currentID = 0;
	
	private static int getNewID() {
		return currentID++;
	}
	
	private static final Player[] players;
	private static final int MAX_PLAYERS = 100;
	
	static {
		players = new Player[MAX_PLAYERS];
	}
	
	public static final Player getPlayer(int id) {
		if(id >= 0 && id < MAX_PLAYERS) return players[id];
		return null;
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
	
}
