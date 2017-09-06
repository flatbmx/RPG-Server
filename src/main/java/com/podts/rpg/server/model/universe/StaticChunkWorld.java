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
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.network.Stream;
import com.podts.rpg.server.network.packet.EntityPacket;
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
		public String toString() {
			return "ChunkCoord - " + x + ", " + y + ", " + z;
		}

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

		protected ChunkCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public ChunkCoordinate move(int dx, int dy, int dz) {
			return new ChunkCoordinate(x + dx, y + dy, z + dz);
		}
	}

	private final class Chunk {

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
		
		Collection<Entity> getEntities() {
			return entities.values();
		}
		
		synchronized Tile getTile(SLocation point) {
			return tiles[point.x - topLeft.x][point.y - topLeft.y];
		}
		
		void addEntity(Entity entity) {
			entities.put(entity.getID(), entity);
		}
		
		void removeEntity(Entity entity) {
			entities.remove(entity.getID());
		}
		
		void addPlayer(Player player) {
			players.put(player.getID(), player);
		}
		
		void removePlayer(Player player) {
			players.remove(player.getID());
		}
		
		protected Chunk(ChunkCoordinate coord) {
			this.coord = coord;
			int x = coord.x * CHUNK_SIZE - (CHUNK_SIZE-1)/2;
			int y = coord.y * CHUNK_SIZE - (CHUNK_SIZE-1)/2;
			topLeft = new SLocation(this, x, y, coord.z);
		}

	}

	private final class MLocation extends Location {

		private int x, y, z;

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

		@Override
		public Location move(int dx, int dy, int dz) {
			return new MLocation(x + dx, y + dy, z + dz);
		}

		MLocation(int nx, int ny, int nz) {
			x = nx;
			y = ny;
			z = nz;
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
			return x + ", " + y + ", " + z;
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
		final Set<ChunkCoordinate> seenCoords = new HashSet<>();
		final Set<Chunk> result = new HashSet<>();
		for(ChunkCoordinate coord : coords) {
			if(seenCoords.contains(coord)) continue;
			result.add(getOrGenerateChunk(coord));
			seenCoords.add(coord);
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
			getWorldGenerator().doGenerateRectTiles(chunk.tiles, chunk.topLeft);
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
	public Tile doGetTile(final Location point) {
		final Chunk chunk = getOrGenerateChunk(getCoordinateFromLocation(point));
		final Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		synchronized(chunk) {
			return chunk.tiles[x][y];
		}
	}

	@Override
	protected World doGetTiles(Tile[][] tiles, Location topLeft) {
		SLocation tL = (SLocation) topLeft;
		Chunk topLeftChunk = tL.chunk;
		Chunk topRightChunk = tL.move(tiles.length, 0, 0).chunk;
		int chunkWidth = topRightChunk.coord.x - topLeftChunk.coord.x;
		Chunk bottomLeftChunk = tL.move(0, tiles[0].length, 0).chunk;
		int chunkHeight = bottomLeftChunk.coord.y - topLeftChunk.coord.y;
		final int width = tiles.length, height = tiles[0].length;
		MLocation loc = new MLocation(tL.x,tL.y, topLeft.getZ());
		for(int i=0; i<=chunkWidth; ++i) {
			for(int j=0; j<chunkHeight; ++j) {
				Chunk chunk = getOrGenerateChunk(topLeftChunk.coord.move(i,j,0));
				for(int dy=loc.getY() - chunk.topLeft.getY(); dy<CHUNK_SIZE; ++dy) {
					for(int dx=loc.getX() - chunk.topLeft.getX(); dx<CHUNK_SIZE; ++dx) {
						//TODO Implement this, holy shit!
					}
				}
			}
		}
		return this;
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
		throw new UnsupportedOperationException("Not implmented yet.");
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
		if(e.isPlayer()) {
			PlayerEntity pE = (PlayerEntity) e;
			initPlayer(pE);
		}
		entities.put(e.getID(), e);
		synchronized(chunk) {
			chunk.addEntity(e);
			if(e.isPlayer()) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.addPlayer(pE.getPlayer());
			}
		}
		return true;
	}

	private void initPlayer(PlayerEntity pE) {
		Player player = pE.getPlayer();
		player.sendPacket(EntityPacket.constructCreate(pE));
		Chunk[][] chunks = getSurroundingChunks((SLocation)pE.getLocation());
		for(int i=0; i<3; ++i) {
			for(int j=0; j<3; ++j) {
				sendNewChunk(chunks[i][j], player);
			}
		}
	}
	
	private void sendNewChunk(Chunk chunk, Player player) {
		player.sendPacket(new TilePacket(chunk.tiles, TileUpdateType.CREATE));
		for(Entity entity : chunk.getEntities()) {
			player.sendPacket(EntityPacket.constructCreate(entity));
		}
	}
	
	@Override
	public boolean doDeRegister(Entity e) {
		if(entities.remove(e.getID()) == null) return false;
		final Chunk chunk = ((SLocation)e.getLocation()).chunk;
		synchronized(chunk) {
			chunk.removeEntity(e);
			if(e.isPlayer()) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.removePlayer(pE.getPlayer());
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
	public void doRegisterRegion(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.add(r);
		}
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

		final Chunk oldChunk = currentLoc.chunk;
		final Chunk newChunk = newLoc.chunk;

		if(oldChunk != newChunk) {
			oldChunk.removeEntity(entity);
			newChunk.addEntity(entity);
			Collection<Player> oldPlayers = getNearbyPlayers(currentLoc);
			Collection<Player> newPlayers = getNearbyPlayers(newLoc);
			
			EntityPacket destroyPacket = EntityPacket.constructDestroy(entity);
			EntityPacket createPacket = EntityPacket.constructCreate(entity);
			
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				Player player = pE.getPlayer();
				oldChunk.removePlayer(pE.getPlayer());
				newChunk.addPlayer(pE.getPlayer());
				int dx = newChunk.coord.x - oldChunk.coord.x;
				int dy = newChunk.coord.y - oldChunk.coord.y;
				Stream stream = pE.getPlayer().getStream();
				
				for(Player oP : oldPlayers) {
					if(player.equals(oP) || newPlayers.contains(oP)) continue;
					oP.sendPacket(destroyPacket);
					player.sendPacket(EntityPacket.constructDestroy(oP.getEntity()));
				}
				
				for(Player nP : newPlayers) {
					if(player.equals(nP) || oldPlayers.contains(nP)) continue;
					nP.sendPacket(createPacket);
					player.sendPacket(EntityPacket.constructCreate(nP.getEntity()));
				}
				
				if(dx != 0) {
					for(int y=-1; y<2; ++y) {
						Chunk tempOldChunk = getOrGenerateChunk(new ChunkCoordinate(currentLoc.chunk.coord.x + dx*-1, currentLoc.chunk.coord.y + y, currentLoc.z));
						Chunk tempNewChunk = getOrGenerateChunk(new ChunkCoordinate(newLoc.chunk.coord.x + dx, newLoc.chunk.coord.y + y, currentLoc.z));
						stream.sendPacket(new TilePacket(tempOldChunk.tiles, TileUpdateType.DESTROY));
						stream.sendPacket(new TilePacket(tempNewChunk.tiles, TileUpdateType.CREATE));
					}
				}
				if(dy != 0) {
					for(int x=-1; x<2; ++x) {
						Chunk tempOldChunk = getOrGenerateChunk(new ChunkCoordinate(currentLoc.chunk.coord.x + x, currentLoc.chunk.coord.y + dy*-1, currentLoc.z));
						Chunk tempNewChunk = getOrGenerateChunk(new ChunkCoordinate(newLoc.chunk.coord.x + x, newLoc.chunk.coord.y + dy, currentLoc.z));
						stream.sendPacket(new TilePacket(tempOldChunk.tiles, TileUpdateType.DESTROY));
						stream.sendPacket(new TilePacket(tempNewChunk.tiles, TileUpdateType.CREATE));
					}
				}
			} else {
				for(Player oP : oldPlayers) {
					if(newPlayers.contains(oP)) continue;
					oP.sendPacket(destroyPacket);
				}
				
				for(Player nP : newPlayers) {
					if(oldPlayers.contains(nP)) continue;
					nP.sendPacket(createPacket);
				}
			}
		}
		entity.setLocation(newLocation);
		return this;
	}

	protected StaticChunkWorld(String name, WorldGenerator generator) {
		super(name, generator);
	}

}
