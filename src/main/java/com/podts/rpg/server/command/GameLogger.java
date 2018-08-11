package com.podts.rpg.server.command;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class GameLogger extends Logger {
	
	protected Handler createHandler() {
		return new ConsoleHandler();
	}
	
	public GameLogger(String name, String bundle) {
		super(name, bundle);
		Handler handler = createHandler();
		handler.setFormatter(new LogFormatter());
		addHandler(handler);
	}
	
	public GameLogger(String name) {
		this(name, null);
	}
	
}
