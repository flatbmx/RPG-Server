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
	
	protected Packet() {
		origin = null;
	}
	
	protected Packet(NetworkStream origin) {
		this.origin = origin;
	}
	
}
