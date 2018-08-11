package com.podts.rpg.server.command;

import java.util.logging.Logger;

import com.podts.rpg.server.network.NetworkStream;

public class NetworkConsole extends Console {
	
	private NetworkConsole(Logger logger, NetworkStream networkStream) {
		super(logger, networkStream.getAddress().getHostAddress());
	}
	
	public static final NetworkConsole construct(NetworkStream networkStream) {
		return new NetworkConsole(new NetworkLogger(networkStream), networkStream);
	}
	
}
