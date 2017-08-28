package com.podts.rpg.server.model.universe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.network.packet.TilePacket;
import com.podts.rpg.server.network.packet.TilePacket.TileUpdateType;

public final class StaticChunkWorld extends World {
	
	private static final int CHUNK_SIZE = 25;
	
	private final Map<Integer,Player> players = new HashMap<>();
	private final Collection<Player> safePlayers = Collections.unmodifiableCollection(players.values());
	private final Map<Integer,Entity> entities = new HashMap<>();
	private final Map<Integer,Map<ChunkCoordinate,Chunk>> chunks = new HashMap<>();
	
	private final static class ChunkCoordinate {
		private int x,y,z;
		@Override
		public int hashCode() {
			return 79254 * 37 + x*25 + y*78 + z*112;
		}
		@Override
		public boolean equals(Object o) {
			if(o == null) return false;
			if(o instanceof ChunkCoordinate) {
				ChunkCoordinate other = (ChunkCoordinate) o;
				return x == other.x && y == other.y && z == other.z;
			}
			return false;
		}
		@Override
		public String toString() {
			return x + ", " + y + ", " + z;
		}
		protected ChunkCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	private class Chunk {
		
		private final ChunkCoordinate coord;
		private final SLocation topLeft;
		
		private final Tile[][] tiles = new Tile[CHUNK_SIZE][CHUNK_SIZE];
		private final Map<Integer,Player> players = new HashMap<>();
		private final Map<Integer,Entity> entities = new HashMap<>();
		private final Set<Region> regions = new HashSet<>(),
				safeRegions = Collections.unmodifiableSet(regions);
		
		@Override
		public String toString() {
			return "Chunk " + coord;
		}
		
		protected Chunk(ChunkCoordinate coord) {
			this.coord = coord;
			int x = coord.x * CHUNK_SIZE - (CHUNK_SIZE-1)/2;
			int y = coord.y * CHUNK_SIZE - (CHUNK_SIZE-1)/2;
			topLeft = new SLocation(this, x, y, coord.z);
		}
		
	}
	
	private final class SLocation extends Location {
		
		private final Chunk chunk;
		private final int x, y, z;
		
		@Override
		public World getWorld() {
			return StaticChunkWorld.this;
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public int getZ() {
			return z;
		}
		
		public final String toString() {
			return "" + x + ", " + y + ", " + z;
		}
		
		@Override
		public SLocation move(int dx, int dy, int dz) {
			SLocation sl = chunk.topLeft;
			int nX = x + dx, nY = y + dy;
			if(nX < sl.x || nY < sl.y || nX - sl.x >= CHUNK_SIZE || nY - sl.y >= CHUNK_SIZE)
				return new SLocation(nX, nY, z + dz);
			return new SLocation(chunk, nX, nY, z + dz);
		}
		
		protected SLocation(Chunk chunk, int x, int y, int z) {
			this.chunk = chunk;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		protected SLocation(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			chunk = getOrGenerateChunkFromLocation(this);
		}
		
	}
	
	private Set<Chunk> getChunksFromLocations(Collection<Location> points) {
		return getChunksFromCoordinates(getCoordinatesFromLocations(points));
	}
	
	private Set<Chunk> getChunksFromCoordinates(Collection<ChunkCoordinate> coords) {
		final Set<Chunk> result = new HashSet<>();
		for(ChunkCoordinate coord : coords) {
			result.add(getOrGenerateChunk(coord));
		}
		return result;
	}
	
	private Set<ChunkCoordinate> getCoordinatesFromLocations(Collection<Location> points) {
		final Set<ChunkCoordinate> coords = new HashSet<ChunkCoordinate>();
		for(Location point : points) {
			coords.add(getCoordinateFromLocation(point));
		}
		return coords;
	}
	
	private Chunk getOrGenerateChunkFromLocation(Location point) {
		return getOrGenerateChunk(getCoordinateFromLocation(point));
	}
	
	private Chunk[][] getSurroundingChunks(SLocation point) {
		Chunk[][] result = new Chunk[3][3];
		ChunkCoordinate center = point.chunk.coord;
		for(int i=-1; i<2; ++i) {
			for(int j=-1; j<2; ++j) {
				result[i+1][j+1] = getOrGenerateChunk(new ChunkCoordinate(center.x + i, center.y + j, point.getZ()));
			}
		}
		return result;
	}
	
	private Chunk getOrGenerateChunk(ChunkCoordinate coord) {
		Map<ChunkCoordinate,Chunk> ch = chunks.get(coord.z);
		if(ch == null) {
			ch = new HashMap<ChunkCoordinate,Chunk>();
			chunks.put(coord.z, ch);
		}
		Chunk chunk = ch.get(coord);
		if(chunk == null) {
			chunk = new Chunk(coord);
			ch.put(coord, chunk);
			getWorldGenerator().generateRectTiles(chunk.tiles, chunk.topLeft);
		}
		return chunk;
	}
	
	private ChunkCoordinate getCoordinateFromLocation(Location point) {
		return getCoordinateFromLocation(point.getX(), point.getY(), point.getZ());
	}
	
	private ChunkCoordinate getCoordinateFromLocation(int x, int y, int z) {
		int cx = (int) Math.floor((x+(CHUNK_SIZE-1)/2d)/CHUNK_SIZE);
		int cy = (int) Math.floor((y+(CHUNK_SIZE-1)/2d)/CHUNK_SIZE);
		return new ChunkCoordinate(cx,cy,z);
	}
	
	@Override
	public Collection<Player> getPlayers() {
		return safePlayers;
	}
	
	@Override
	public Tile getTile(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot get Tile for null location.");
		final Location point = loc.getLocation();
		if(!equals(point.getWorld())) throw new IllegalArgumentException("Cannot get Tile that exists in a different World.");
		
		Chunk chunk = getOrGenerateChunk(getCoordinateFromLocation(point));
		Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		synchronized(chunk) {
			return chunk.tiles[x][y];
		}
	}
	
	public void doSetTile(Tile newTile) {
		Location point = newTile.getLocation();
		Chunk chunk = getOrGenerateChunk(getCoordinateFromLocation(point));
		Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		synchronized(chunk) {
			chunk.tiles[x][y] = newTile;
		}
	}
	
	@Override
	public Collection<Entity> getNearbyEntities(Locatable l, double distance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Collection<Entity> getNearbyEntities(Locatable l) {
		Set<Entity> result = new HashSet<>();
		SLocation point = (SLocation) l.getLocation();
		Chunk[][] chunks = getSurroundingChunks(point);
		for(int i=-1; i<2; ++i) {
			for(int j=-1; j<2; ++j) {
				result.addAll(chunks[i][j].entities.values());
			}
		}
		return result;
	}
	
	public Collection<Player> getNearbyPlayers(Locatable l) {
		List<Player> result;
		Chunk[][] chunks = getSurroundingChunks((SLocation) l.getLocation());
		int size = 0;
		for(int i=0; i<3; ++i) {
			for(int j=0; j<3; ++j) {
				size += chunks[i][j].players.size();
			}
		}
		Player[] pArr = new Player[size];
		result = Arrays.asList(pArr);
		size = 0;
		for(int i=0; i<3; ++i) {
			for(int j=0; j<3; ++j) {
				for(Player player : chunks[i][j].players.values()) {
					pArr[size++] = player;
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean doRegister(Entity e) {
		if(entities.containsKey(e.getID())) return false;
		final Chunk chunk = ((SLocation)e.getLocation()).chunk;
		entities.put(e.getID(), e);
		synchronized(chunk) {
			chunk.entities.put(e.getID(), e);
			if(e.isPlayer()) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.players.put(pE.getPlayer().getID(), pE.getPlayer());
			}
		}
		if(e.isPlayer()) {
			PlayerEntity pE = (PlayerEntity) e;
			initPlayer(pE);
		}
		return true;
	}
	
	private void initPlayer(PlayerEntity pE) {
		Player player = pE.getPlayer();
		Chunk[][] chunks = getSurroundingChunks((SLocation)pE.getLocation());
		for(int i=0; i<3; ++i) {
			for(int j=0; j<3; ++j) {
				TilePacket packet = new TilePacket(chunks[i][j].tiles, TileUpdateType.CREATE);
				player.getStream().sendPacket(packet);
			}
		}
	}
	
	@Override
	public boolean doDeRegister(Entity e) {
		if(entities.remove(e.getID()) == null) return false;
		final Chunk chunk = ((SLocation)e.getLocation()).chunk;
		synchronized(chunk) {
			chunk.entities.remove(e.getID());
			if(e.isPlayer()) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.players.remove(pE.getPlayer().getID());
			}
		}
		return true;
	}
	
	@Override
	public Collection<Region> getRegionsAtLocation(Locatable loc) {
		return getOrGenerateChunkFromLocation(loc.getLocation()).safeRegions;
	}

	@Override
	public Entity getEntity(int id) {
		return entities.get(id);
	}

	@Override
	public StaticChunkWorld registerRegion(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.add(r);
		}
		return this;
	}

	@Override
	public StaticChunkWorld deRegisterRegion(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.remove(r);
		}
		return this;
	}

	@Override
	public Collection<Entity> getEntitiesInRegion(PollableRegion r) {
		Set<Entity> result = new HashSet<>();
		for(Location point : r.getPoints()) {
			result.addAll(getEntitiesAtLocation(point));
		}
		return result;
	}

	private Collection<Entity> getEntitiesAtLocation(Location point) {
		Set<Entity> result = new HashSet<>();
		Chunk chunk = getOrGenerateChunkFromLocation(point);
		for(Entity e : chunk.entities.values()) {
			if(e.getLocation().equals(point)) result.add(e);
		}
		return result;
	}

	@Override
	public SLocation createLocation(int x, int y, int z) {
		return new SLocation(x, y, z);
	}
	
	@Override
	protected World doMoveEntity(Entity entity, Location newLocation, MoveType type) {
		SLocation newLoc = (SLocation) newLocation;
		SLocation currentLoc = (SLocation) entity.getLocation();
		
		if(newLoc.chunk == currentLoc.chunk) {
			entity.setLocation(newLocation);
		} else {
			System.out.println("Moved from " + currentLoc.chunk + " to " + newLoc.chunk);
			currentLoc.chunk.entities.remove(entity.getID());
			newLoc.chunk.entities.put(entity.getID(), entity);
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				currentLoc.chunk.players.remove(pE.getPlayer().getID());
				newLoc.chunk.players.put(pE.getPlayer().getID(), pE.getPlayer());
				int dx = newLoc.chunk.coord.x - currentLoc.chunk.coord.x;
				int dy = newLoc.chunk.coord.y - currentLoc.chunk.coord.y;
				if(dx != 0) {
					for(int y=-1; y<2; ++y) {
						Chunk oldChunk = getOrGenerateChunk(new ChunkCoordinate(currentLoc.chunk.coord.x + dx*-1, currentLoc.chunk.coord.y + y, currentLoc.z));
						Chunk newChunk = getOrGenerateChunk(new ChunkCoordinate(newLoc.chunk.coord.x + dx, newLoc.chunk.coord.y + y, currentLoc.z));
						pE.getPlayer().getStream().sendPacket(new TilePacket(oldChunk.tiles, TileUpdateType.DESTROY));
						pE.getPlayer().getStream().sendPacket(new TilePacket(newChunk.tiles, TileUpdateType.CREATE));
					}
				}
				if(dy != 0) {
					for(int x=-1; x<2; ++x) {
						Chunk oldChunk = getOrGenerateChunk(new ChunkCoordinate(currentLoc.chunk.coord.x + x, currentLoc.chunk.coord.y + dy*-1, currentLoc.z));
						Chunk newChunk = getOrGenerateChunk(new ChunkCoordinate(newLoc.chunk.coord.x + x, newLoc.chunk.coord.y + dy, currentLoc.z));
						pE.getPlayer().getStream().sendPacket(new TilePacket(oldChunk.tiles, TileUpdateType.DESTROY));
						pE.getPlayer().getStream().sendPacket(new TilePacket(newChunk.tiles, TileUpdateType.CREATE));
					}
				}
			}
			entity.setLocation(newLoc);
		}
		return this;
	}
	
	protected StaticChunkWorld(String name, WorldGenerator generator) {
		super(name, generator);
	}
	
}
