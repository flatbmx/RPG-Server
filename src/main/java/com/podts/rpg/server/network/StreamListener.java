package com.podts.rpg.server.network;

public interface StreamListener {
	
	public default void onConnect(Stream stream) {
		
	}
	
	public default void onDisconnect(Stream stream) {
		
	}
	
}
