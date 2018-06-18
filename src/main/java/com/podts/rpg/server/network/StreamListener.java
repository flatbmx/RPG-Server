package com.podts.rpg.server.network;

public interface StreamListener {
	
	public default void onConnect(NetworkStream networkStream) {
		
	}
	
	public default void onDisconnect(NetworkStream networkStream) {
		
	}
	
}
