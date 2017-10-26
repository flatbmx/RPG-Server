package com.podts.rpg.server.network.packet;

import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.network.Packet;

public final class TilePacket extends Packet {
	
	public enum TileUpdateType {
		CREATE(),
		DESTROY();
	}
	
	public enum TileSendType {
		GROUP(),
		SINGLE();
	}
	
	private final TileUpdateType updateType;
	private final TileSendType sendType;
	private final Tile tile;
	private final Tile[][] tiles;
	
	public TileUpdateType getUpdateType() {
		return updateType;
	}
	
	public TileSendType getSendType() {
		return sendType;
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public Tile[][] getTiles() {
		return tiles;
	}
	
	public static final TilePacket constructCreate(Tile tile) {
		return new TilePacket(tile, TileUpdateType.CREATE);
	}
	
	public static final TilePacket constructCreate(Tile[][] tiles) {
		return new TilePacket(tiles, TileUpdateType.CREATE);
	}
	
	public static final TilePacket constructDestroy(Tile tile) {
		return new TilePacket(tile, TileUpdateType.DESTROY);
	}
	
	public static final TilePacket constructDestroy(Tile[][] tiles) {
		return new TilePacket(tiles, TileUpdateType.DESTROY);
	}
	
	TilePacket(Tile tile, TileUpdateType updateType) {
		sendType = TileSendType.SINGLE;
		this.updateType = updateType;
		this.tile = tile;
		tiles = null;
	}
	
	TilePacket(Tile[][] tiles, TileUpdateType updateType) {
		sendType = TileSendType.GROUP;
		this.updateType = updateType;
		this.tiles = tiles;
		tile = null;
	}
	
}
