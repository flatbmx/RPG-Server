package com.podts.rpg.server.command;

import com.podts.rpg.server.Server;

public class LocalConsole extends Console {
	
	public LocalConsole() {
		super(Server.get().getLogger(), "Console");
	}
	
}
