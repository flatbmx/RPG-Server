package com.podts.rpg.server.network;

public abstract class Packet {
	
	private final Stream origin;
	
	public final Stream getOrigin() {
		return origin;
	}
	
	public final boolean isInbound() {
		return getOrigin() != null;
	}
	
	public void handle() {
		
	}
	
	public Packet() {
		origin = null;
	}
	
	public Packet(Stream origin) {
		this.origin = origin;
	}
	
}
