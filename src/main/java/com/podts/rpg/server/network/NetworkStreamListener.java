package com.podts.rpg.server.network;

public interface NetworkStreamListener {
	
	public default void onConnect(NetworkStream networkStream) {
		
	}
	
	public default void onDisconnect(NetworkStream networkStream) {
		
	}
	
}
