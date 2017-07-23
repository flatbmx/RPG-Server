package com.podts.rpg.server.model;

import java.util.LinkedList;
import java.util.List;

public class Galaxy {
	
	private static Galaxy instance;
	
	private static final System sol = new System("Sol", 10_000);
	private static final Location startingLocation = new Location(sol);
	
	public static final Galaxy get() {
		if(instance == null)
			instance = new Galaxy();
		return instance;
	}
	
	public static final Location getStartingLocation() {
		return startingLocation;
	}
	
	private final List<PlayerLoginHandler> loginHandlers = new LinkedList<PlayerLoginHandler>();
	
	public Galaxy addPlayerLoginHandler(PlayerLoginHandler handler) {
		if(handler == null) throw new IllegalArgumentException("Cannot add a null login handler.");
		loginHandlers.add(handler);
		return this;
	}
	
	public Galaxy removePlayerLoginHandler(PlayerLoginHandler handler) {
		loginHandlers.remove(handler);
		return this;
	}
	
	public Galaxy handleLogin(Player player) {
		for(PlayerLoginHandler handler : loginHandlers) {
			handler.onPlayerLogin(player);
		}
		return this;
	}
	
	public Galaxy handleLogOut(Player player) {
		for(PlayerLoginHandler handler : loginHandlers) {
			handler.onPlayerLogout(player);
		}
		return this;
	}
	
	private Galaxy() {
		if(instance == null)
			instance = this;
	}
	
}
