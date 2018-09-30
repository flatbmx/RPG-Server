package com.podts.rpg.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import com.podts.rpg.server.command.CommandSender;
import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.MessagePacket;
import com.podts.rpg.server.network.packet.StatePacket;
import com.podts.rpg.server.network.packet.TileSelectionPacket;

public class Player implements CommandSender {
	
	public static final boolean is(Locatable l) {
		return l instanceof PlayerEntity;
	}
	
	public static final boolean is(Entity e) {
		if(e == null) return false;
		return EntityType.PLAYER.equals(e.getType());
	}
	
	public static final Player get(Entity e) {
		if(e instanceof PlayerEntity) {
			PlayerEntity pE = (PlayerEntity) e;
			return pE.getPlayer();
		}
		return null;
	}
	
	private final int id;
	private String username, password;
	private PlayerEntity entity;
	
	private GameState currentState;
	private NetworkStream networkStream;
	
	private final Collection<Tile> selectedTiles = new HashSet<>();
	private final Collection<Tile> safeSelectedTiles = Collections.unmodifiableCollection(selectedTiles);
	
	public final int getID() {
		return id;
	}
	
	@Override
	public final String getName() {
		return username;
	}
	
	public Collection<Tile> getSelectedTiles() {
		return safeSelectedTiles;
	}
	
	public Player setSelectedTiles(Collection<Tile> newTiles, boolean update) {
		selectedTiles.clear();
		selectedTiles.addAll(newTiles);
		if(update)
			sendSelectedTiles();
		return this;
	}
	
	public Player setSelectedTiles(Collection<Tile> newTiles) {
		return setSelectedTiles(newTiles, true);
	}
	
	public Player selectTile(Tile tile) {
		selectedTiles.add(tile);
		sendSelectedTiles();
		return this;
	}
	
	public Player deSelectTile(Tile tile) {
		selectedTiles.remove(tile);
		sendSelectedTiles();
		return this;
	}
	
	public Player clearSelectedTiles() {
		if(selectedTiles.isEmpty()) {
			selectedTiles.clear();
			sendSelectedTiles();
		}
		return this;
	}
	
	public boolean isSelected(Tile tile) {
		return getSelectedTiles().contains(tile);
	}
	
	private void sendSelectedTiles() {
		sendPacket(new TileSelectionPacket(selectedTiles));
	}
	
	public final GameState getGameState() {
		return currentState;
	}
	
	public final boolean isInGameState(GameState state) {
		return getGameState().equals(state);
	}
	
	public final Player changeGameState(final GameState newState) {
		Objects.requireNonNull(newState, "Cannot switch player to a null GameState!");
		final GameState oldState = currentState;
		currentState = newState;
		sendPacket(new StatePacket(newState));
		newState.onEnter(this, oldState);
		return this;
	}
	
	public final NetworkStream getStream() {
		return networkStream;
	}
	
	//TODO REALLY REALLY need to change this scope.
	public final void setStream(NetworkStream s) {
		networkStream = s;
	}
	
	public final String getUsername() {
		return username;
	}
	
	public final String getPassword() {
		return password;
	}
	
	public final PlayerEntity getEntity() {
		return entity;
	}
	
	//TODO REALLY need to change this scope.
	public void setEntity(PlayerEntity e) {
		entity = e;
	}

	public final void sendPacket(Packet... packets) {
		for(Packet p : packets)
			getStream().sendPacket(p);
	}
	
	@Override
	public final void sendMessage(String message) {
		sendPacket(new MessagePacket(message));
	}
	
	@Override
	public final void sendMessage(CommandSender sender, String message) {
		sendPacket(new MessagePacket(sender, message));
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o instanceof Player) {
			Player other = (Player) o;
			return id == other.id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	Player(int id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
		currentState = GameStates.NONE;
	}
	
	public static enum LogoutReason {
		DISCONNECT(),
		LOGOUT();
	}
	
}
