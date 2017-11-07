package com.podts.rpg.server.command;

import java.util.logging.Logger;

import com.podts.rpg.server.network.Stream;

public class NetworkConsole extends Console {
	
	private NetworkConsole(Logger logger, Stream stream) {
		super(logger, stream.getAddress().getHostAddress());
	}
	
	public static final NetworkConsole construct(Stream stream) {
		return new NetworkConsole(new NetworkLogger(stream,"bundle"), stream);
	}
	
}
