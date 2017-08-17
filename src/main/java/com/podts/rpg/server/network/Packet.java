package com.podts.rpg.server.network;

public abstract class Packet {
	
	private Stream origin;
	
	public final Stream getOrigin() {
		return origin;
	}
	
	protected final void setStream(Stream newStream) {
		origin = newStream;
	}
	
	public final boolean isInbound() {
		return getOrigin() != null;
	}
	
	public Packet() {
		origin = null;
	}
	
	public Packet(Stream origin) {
		this.origin = origin;
	}
	
}
