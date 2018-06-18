package com.podts.rpg.server.network;

public abstract class Packet {
	
	private NetworkStream origin;
	
	public final NetworkStream getOrigin() {
		return origin;
	}
	
	protected final void setStream(NetworkStream newStream) {
		origin = newStream;
	}
	
	public final boolean isInbound() {
		return getOrigin() != null;
	}
	
	public Packet() {
		origin = null;
	}
	
	public Packet(NetworkStream origin) {
		this.origin = origin;
	}
	
}
