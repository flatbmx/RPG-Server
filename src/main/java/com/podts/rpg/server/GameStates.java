package com.podts.rpg.server;

import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.network.packet.PlayerInitPacket;

public class GameStates {
	
	public static final NoState NONE = new NoState();
	public static final LoggingInState LOGGING_IN = new LoggingInState();
	public static final PlayingState PLAYING = new PlayingState();
	
	public static class NoState extends GameState {
		
		protected void onEnter(Player player, GameState previous) {}
		protected void onLeave(Player player, GameState next) {}
		
		private NoState() {
			super(-1, "No");
		}
		
	}
	
	public static class LoggingInState extends GameState {
		
		protected void onEnter(Player player, GameState previous) {
			player.sendPacket(new PlayerInitPacket(player));
			Universe.get().getDefaultWorld().register(player.getEntity());
			Server.get().handleLogin(player);
			player.changeGameState(GameStates.PLAYING);
		}
		
		protected void onLeave(Player player, GameState next) {}
		
		private LoggingInState() {
			super(0, "Login");
		}
		
	}
	
	public static class PlayingState extends GameState {

		@Override
		protected void onEnter(Player player, GameState previous) {
			player.sendMessage("Welcome " + player + "!");
		}

		@Override
		protected void onLeave(Player player, GameState next) {
			
		}
		
		private PlayingState() {
			super(1, "Playing");
		}
		
	}
	
}
