package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.TilePacket;

public final class StaticChunkWorld extends World {
	
	private static final int DEFAULT_CHUNK_SIZE = 25;
	private static final int DEFAULT_CHUNK_DEPTH = 1;
	
	private final Map<Integer,Player> players = new HashMap<>();
	private final Collection<Player> safePlayers = Collections.unmodifiableCollection(players.values());
	private final Map<Integer,Entity> entities = new HashMap<>();
	private final Map<Integer,Map<ChunkCoordinate,Chunk>> chunks = new HashMap<>();
	
	private final Collection<PollableRegion> registeredRegions = new HashSet<>();
	
	private final int chunkSize;
	private final int chunkDepth;
	
	private final static class ChunkCoordinate {
		
		private final int x,y,z;
		private final int hash;

		@Override
		public String toString() {
			return "ChunkCoord - " + x + ", " + y + ", " + z;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o instanceof ChunkCoordinate) {
				ChunkCoordinate other = (ChunkCoordinate) o;
				return x == other.x && y == other.y && z == other.z;
			}
			return false;
		}
		
		private final int computeHash() {
			return 79254 + x*37 + y*78 + z*113;
		}
		
		protected ChunkCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			hash = computeHash();
		}
		
		public ChunkCoordinate move(int dx, int dy, int dz) {
			return new ChunkCoordinate(x + dx, y + dy, z + dz);
		}
	}

	private final class Chunk {
		
		private final ChunkCoordinate coord;
		private final SLocation topLeft;
		
		boolean generated = false;

		private final Tile[][] tiles = new Tile[getChunkSize()][getChunkSize()];
		private final Map<Integer,Player> players = new HashMap<>();
		private final Map<Integer,Entity> entities = new HashMap<>();
		private final Set<Region> regions = new HashSet<>(),
				safeRegions = Collections.unmodifiableSet(regions);
		
		@Override
		public String toString() {
			return "Chunk " + coord;
		}
		
		final int size() {
			return StaticChunkWorld.this.chunkSize;
		}
		
		Collection<Entity> getEntities() {
			return entities.values();
		}
		
		StaticChunkWorld getWorld() {
			return StaticChunkWorld.this;
		}
		
		Tile getTile(SLocation point) {
			return tiles[point.x - topLeft.x][point.y - topLeft.y];
		}
		
		Tile getTile(int x, int y) {
			return tiles[x][y];
		}
		
		void setTile(Tile newTile, int x, int y) {
			tiles[x][y] = newTile;
		}
		
		void addEntity(Entity entity) {
			entities.put(entity.getID(), entity);
		}
		
		void addEntity(PlayerEntity entity) {
			entities.put(entity.getID(), entity);
			players.put(entity.getPlayer().getID(), entity.getPlayer());
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
		
		final boolean isGenerated() {
			return generated;
		}
		
		final Stream<Tile> tiles() {
			final Iterator<Tile> source = new TileIterator();
			Iterable<Tile> iterable = () -> source;
			return StreamSupport.stream(iterable.spliterator(), false);
		}
		
		private class TileIterator implements Iterator<Tile> {
			
			private int x, y;
			
			@Override
			public boolean hasNext() {
				return !(y == (size()-1) && x == (size()-1));
			}
			
			@Override
			public Tile next() {
				if(!hasNext()) throw new NoSuchElementException();
				Tile result = tiles[x++][y];
				if(x == size()) {
					x = 0;
					++y;
				}
				return result;
			}
			
		}
		
		protected Chunk(ChunkCoordinate coord) {
			this.coord = coord;
			int x = coord.x * getChunkSize() - (getChunkSize()-1)/2;
			int y = coord.y * getChunkSize() - (getChunkSize()-1)/2;
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

		private Chunk chunk;
		private final int x, y, z;
		private final int hash;

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
		public final String toString() {
			return x + ", " + y + ", " + z;
		}
		
		@Override
		public SLocation move(final int dx, final int dy, final int dz) {
			final SLocation sl = getChunk().topLeft;
			final int nX = x + dx, nY = y + dy;
			if(nX < sl.x || nY < sl.y || nX - sl.x >= getChunkSize() || nY - sl.y >= getChunkSize())
				return new SLocation(nX, nY, z + dz);
			return new SLocation(chunk, nX, nY, z + dz);
		}
		
		@Override
		public final int hashCode() {
			return hash;
		}
		
		@Override
		public final boolean equals(Object o) {
			if(this == o) return true;
			if(o == null) return false;
			if(!(o instanceof SLocation)) return false;
			SLocation other = (SLocation) o;
			return getX() == other.getX() && getY() == other.getY() && getZ() == other.getZ();
		}
		
		Chunk getChunk() {
			if(chunk == null) {
				chunk = StaticChunkWorld.this.findChunk(this);
			}
			return chunk;
		}
		
		protected SLocation(Chunk chunk, int x, int y, int z) {
			this.chunk = chunk;
			this.x = x;
			this.y = y;
			this.z = z;
			hash = 79254 * 31 + x*17 + y*77 + z*111;
		}

		protected SLocation(int x, int y, int z) {
			this(null, x, y, z);
		}

	}
	
	private int getChunkSize() {
		return chunkSize;
	}
	
	private int getChunkDepth() {
		return chunkDepth;
	}
	
	private Set<Chunk> getChunksFromLocations(Collection<Location> points) {
		return getChunksFromCoordinates(getCoordinatesFromLocations(points));
	}

	private void getChunksFromCoordinates(final Collection<ChunkCoordinate> coords, final Set<Chunk> chunks) {
		for(final ChunkCoordinate coord : coords) {
			chunks.add(getOrGenerateChunk(coord));
		}
	}
	
	private Set<Chunk> getChunksFromCoordinates(final Collection<ChunkCoordinate> coords) {
		final Set<Chunk> result = new HashSet<>();
		getChunksFromCoordinates(coords, result);
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
	
	private Chunk[][] getSurroundingChunks(SLocation point, int depth) {
		int arraySize = 1 + depth * 2;
		Chunk[][] result = new Chunk[arraySize][arraySize];
		ChunkCoordinate center = point.getChunk().coord;
		for(int i=-depth; i<depth+1; ++i) {
			for(int j=-depth; j<depth+1; ++j) {
				result[i+1][j+1] = getOrGenerateChunk(center.move(i, j, 0));
			}
		}
		return result;
	}
	
	/**
	 * Returns the surrounding chunks around the given point using the worlds chunk depth.
	 * @param point
	 * @return
	 */
	private Chunk[][] getSurroundingChunks(final SLocation point) {
		return getSurroundingChunks(point, getChunkDepth());
	}
	
	private Chunk findChunk(final SLocation point) {
		return findChunk(getCoordinateFromLocation(point));
	}
	
	private Chunk findChunk(final ChunkCoordinate coord) {
		Map<ChunkCoordinate,Chunk> ch = chunks.get(coord.z);
		if(ch == null) {
			ch = new HashMap<ChunkCoordinate,Chunk>();
			chunks.put(coord.z, ch);
		}
		Chunk chunk = ch.get(coord);
		if(chunk == null) {
			chunk = new Chunk(coord);
			ch.put(coord, chunk);
		}
		return chunk;
	}
	
	private Chunk getOrGenerateChunk(final ChunkCoordinate coord) {
		final Chunk chunk = findChunk(coord);
		checkGenerateChunk(chunk);
		return chunk;
	}

	private ChunkCoordinate getCoordinateFromLocation(final Location point) {
		if(point instanceof SLocation) return ((SLocation)point).getChunk().coord;
		return getCoordinateFromLocation(point.getX(), point.getY(), point.getZ());
	}

	private ChunkCoordinate getCoordinateFromLocation(final int x, final int y, final int z) {
		int cx = (int) Math.floor((x+(getChunkSize()-1)/2d)/getChunkSize());
		int cy = (int) Math.floor((y+(getChunkSize()-1)/2d)/getChunkSize());
		return new ChunkCoordinate(cx,cy,z);
	}
	
	private void findRegionChunkCooridinates(final PollableRegion region, final Set<ChunkCoordinate> collection) {
		for(final Location point : region.getPoints()) {
			collection.add(getCoordinateFromLocation(point));
		}
	}
	
	/**
	 * If the given chunk is not generated generate it.
	 * @param chunk
	 */
	private final void checkGenerateChunk(final Chunk chunk) {
		if(!chunk.isGenerated()) generateChunk(chunk);
	}
	
	/**
	 * Generates the given chunk using this worlds world generator.
	 * NOTE: This will re-generate already generated chunks.
	 * @param chunk
	 */
	private void generateChunk(final Chunk chunk) {
		getWorldGenerator().doGenerateRectTiles(chunk.tiles, chunk.topLeft);
		chunk.generated = true;
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
		return chunk.getTile(x, y);
	}

	@Override
	protected void doGetTiles(Tile[][] tiles, Location topLeft) {
		SLocation tL = (SLocation) topLeft;
		Chunk topLeftChunk = tL.getChunk();
		Chunk topRightChunk = tL.move(tiles.length, 0, 0).getChunk();
		int chunkWidth = topRightChunk.coord.x - topLeftChunk.coord.x;
		Chunk bottomLeftChunk = tL.move(0, tiles[0].length, 0).getChunk();
		int chunkHeight = bottomLeftChunk.coord.y - topLeftChunk.coord.y;
		final int width = tiles.length, height = tiles[0].length;
		MLocation loc = new MLocation(tL.x,tL.y, topLeft.getZ());
		for(int i=0; i<=chunkWidth; ++i) {
			for(int j=0; j<chunkHeight; ++j) {
				Chunk chunk = getOrGenerateChunk(topLeftChunk.coord.move(i,j,0));
				for(int dy=loc.getY() - chunk.topLeft.getY(); dy<getChunkSize(); ++dy) {
					for(int dx=loc.getX() - chunk.topLeft.getX(); dx<getChunkSize(); ++dx) {
						//TODO Implement this, holy shit!
					}
				}
			}
		}
	}
	
	@Override
	protected void doSetTile(Tile newTile) {
		Location point = newTile.getLocation();
		Chunk chunk = getOrGenerateChunk(getCoordinateFromLocation(point));
		Location topLeft = chunk.topLeft;
		int x = point.getX() - topLeft.getX();
		int y = point.getY() - topLeft.getY();
		chunk.setTile(newTile, x, y);
	}

	@Override
	public Collection<Entity> doGetNearbyEntities(Location point, double distance, Predicate<Entity> condition) {
		int surroundingChunkDepth = (int) Math.floor(getChunkSize()/distance);
		Chunk[][] chunks = getSurroundingChunks((SLocation)point, surroundingChunkDepth);
		Collection<Entity> result = new HashSet<>();
		
		for(int i=0; i<chunks.length; ++i) {
			for(int j=0; j<chunks[i].length; ++j) {
				if(condition == null) {
					for(Entity e : chunks[i][j].entities.values()) {
						if(point.distance(e) <= distance) result.add(e);
					}
				} else {
					for(Entity e : chunks[i][j].entities.values()) {
						if(point.distance(e) <= distance && condition.test(e)) result.add(e);
					}
				}
				
			}
		}
		return result;
	}

	@Override
	public Collection<Entity> doGetNearbyEntities(Location point, Predicate<Entity> condition) {
		Set<Entity> result = new HashSet<>();
		SLocation spoint = (SLocation) point;
		Chunk[][] chunks = getSurroundingChunks(spoint);
		for(int i=0; i<chunks.length; ++i) {
			for(int j=0; j<chunks[i].length; ++j) {
				if(condition == null) {
					result.addAll(chunks[i][j].entities.values());
				} else {
					for(Entity e : chunks[i][j].entities.values()) {
						if(condition.test(e)) result.add(e);
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public Stream<Entity> getNearbyEntitiesStream(Locatable l) {
		Utils.assertArg(!contains(l), "Cannot get nearby Entity Stream from location in another world.");
		Chunk[][] chunks = getSurroundingChunks((SLocation)l.getLocation());
		Stream<Entity> stream = Stream.empty();
		for(int i=0; i<chunks.length; ++i) {
			for(int j=0; j<chunks[i].length; ++j) {
				stream = Stream.concat(stream, chunks[i][j].entities.values().stream());
			}
		}
		return stream;
	}
	
	@Override
	public Stream<Entity> getNearbyEntitiesStream(Locatable l, double distance) {
		Utils.assertArg(!contains(l), "Cannot get nearby Entity Stream from location in another world.");
		return getNearbyEntities(l, distance).stream();
	}
	
	@Override
	public Collection<Player> doGetNearbyPlayers(Location point) {
		Set<Player> result = new HashSet<>();
		Chunk[][] chunks = getSurroundingChunks((SLocation) point);
		for(int i=0; i<chunks.length; ++i) {
			for(int j=0; j<chunks[i].length; ++j) {
				result.addAll(chunks[i][j].players.values());
			}
		}
		return result;
	}

	@Override
	public boolean doRegister(Entity e) {
		if(isRegistered(e)) return false;
		
		//Add entity to chunk
		final Chunk chunk = ((SLocation)e.getLocation()).getChunk();
		checkGenerateChunk(chunk);
		chunk.addEntity(e);
		
		//If entity is player, add/setup the player.
		if(e.isPlayer()) initPlayer((PlayerEntity) e);
		
		//Add entity to world-wide collection.
		entities.put(e.getID(), e);
		return true;
	}
	
	public final boolean isRegistered(final Entity e) {
		return entities.containsKey(e.getID());
	}
	
	private void addPlayer(Player player) {
		players.put(player.getID(), player);
	}
	
	private void removePlayer(Player player) {
		players.remove(player.getID());
	}
	
	private void initPlayer(PlayerEntity pE) {
		Player player = pE.getPlayer();
		addPlayer(player);
		player.sendPacket(EntityPacket.constructCreate(pE));
		Chunk[][] chunks = getSurroundingChunks((SLocation)pE.getLocation());
		for(int i=0; i<chunks.length; ++i) {
			for(int j=0; j<chunks[i].length; ++j) {
				sendEntireChunk(chunks[i][j], player);
			}
		}
	}
	
	private void sendEntireChunk(Chunk chunk, Player player) {
		sendCreateChunk(player,chunk);
		for(Entity entity : chunk.getEntities()) {
			if(entity.equals(player.getEntity())) continue;
			sendCreateEntity(entity, player);
		}
	}
	
	private void deSendEntireChunk(Chunk chunk, Player player) {
		sendDeleteChunk(player, chunk);
	}
	
	@Override
	public boolean doDeRegister(Entity e) {
		if(entities.remove(e.getID()) == null) return false;
		final Chunk chunk = ((SLocation)e.getLocation()).getChunk();
		synchronized(chunk) {
			chunk.removeEntity(e);
			if(e.isPlayer()) {
				PlayerEntity pE = (PlayerEntity) e;
				chunk.removePlayer(pE.getPlayer());
				removePlayer(pE.getPlayer());
			}
		}
		return true;
	}

	@Override
	public Collection<Region> getRegionsAtLocation(Locatable loc) {
		if(!contains(loc)) throw new IllegalArgumentException("Cannot get Regions from a Location from another World.");
		return findChunk((SLocation)loc.getLocation()).safeRegions;
	}

	@Override
	public Entity getEntity(int id) {
		return entities.get(id);
	}
	
	public final boolean isRegistered(PollableRegion r) {
		return registeredRegions.contains(r);
	}
	
	@Override
	public void doRegister(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.add(r);
		}
		registeredRegions.add(r);
	}
	
	@Override
	public StaticChunkWorld deRegister(PollableRegion r) {
		Set<Chunk> chunks = getChunksFromCoordinates(getCoordinatesFromLocations(r.getPoints()));
		for(Chunk c : chunks) {
			c.regions.remove(r);
		}
		registeredRegions.remove(r);
		return this;
	}
	
	protected Collection<Entity> findEntitiesAtLocation(final Location point, final Collection<Entity> collection) {
		final Chunk chunk = getOrGenerateChunkFromLocation(point);
		for(final Entity e : chunk.entities.values()) {
			if(e.getLocation().equals(point)) collection.add(e);
		}
		return collection;
	}
	
	@Override
	protected void findEntitiesInRegion(final PollableRegion r, final Collection<Entity> collection) {
		final Collection<Chunk> chunks = getChunksFromLocations(r.getPoints());
		for(final Chunk chunk : chunks) {
			for(final Entity e : chunk.getEntities()) {
				if(r.contains(e)) collection.add(e);
			}
		}
	}
	
	@Override
	public SLocation createLocation(final int x, final int y, final int z) {
		return new SLocation(x, y, z);
	}
	
	@Override
	protected World doMoveEntity(Entity entity, Location newLocation, MoveType type) {
		SLocation newLoc = (SLocation) newLocation;
		SLocation currentLoc = (SLocation) entity.getLocation();

		final Chunk oldChunk = currentLoc.getChunk();
		final Chunk newChunk = newLoc.getChunk();

		if(oldChunk != newChunk) {
			checkGenerateChunk(newChunk);
			
			oldChunk.removeEntity(entity);
			newChunk.addEntity(entity);
			final Collection<Player> oldPlayers = getNearbyPlayers(currentLoc);
			final Collection<Player> newPlayers = getNearbyPlayers(newLoc);
			
			final EntityPacket destroyPacket = EntityPacket.constructDestroy(entity);
			final EntityPacket createPacket = EntityPacket.constructCreate(entity);
			
			if(entity instanceof PlayerEntity) {
				PlayerEntity pE = (PlayerEntity) entity;
				Player player = pE.getPlayer();
				oldChunk.removePlayer(pE.getPlayer());
				newChunk.addPlayer(pE.getPlayer());
				int dx = newChunk.coord.x - oldChunk.coord.x;
				int dy = newChunk.coord.y - oldChunk.coord.y;
				
				for(Player oP : oldPlayers) {
					if(newPlayers.contains(oP) || player.equals(oP)) continue;
					oP.sendPacket(destroyPacket);
					player.sendPacket(EntityPacket.constructDestroy(oP.getEntity()));
				}
				
				for(Player nP : newPlayers) {
					if(oldPlayers.contains(nP) || player.equals(nP)) continue;
					nP.sendPacket(createPacket);
					player.sendPacket(EntityPacket.constructCreate(nP.getEntity()));
				}
				
				if(dx != 0) {
					for(int y=-1; y<2; ++y) {
						sendDeleteChunk(player, getOrGenerateChunk(currentLoc.getChunk().coord.move(dx*-1, y, 0)));
						sendCreateChunk(player, getOrGenerateChunk(newLoc.getChunk().coord.move(dx, y, 0)));
					}
				}
				if(dy != 0) {
					for(int x=-1; x<2; ++x) {
						sendDeleteChunk(player, getOrGenerateChunk(currentLoc.getChunk().coord.move(x, dy*-1, 0)));
						sendCreateChunk(player, getOrGenerateChunk(newLoc.getChunk().coord.move(x, dy, 0)));
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
	
	private void sendCreateChunk(final Player player, final Chunk chunk) {
		player.sendPacket(TilePacket.constructCreate(chunk.tiles));
	}
	
	private void sendDeleteChunk(final Player player, final Chunk chunk) {
		player.sendPacket(TilePacket.constructDestroy(chunk.tiles));
	}
	
	protected StaticChunkWorld(final String name, final int chunkSize, final int chunkDepth, final WorldGenerator generator) {
		super(name, generator);
		this.chunkSize = chunkSize;
		this.chunkDepth = chunkDepth;
	}
	
	protected StaticChunkWorld(final String name, final int chunkSize, final WorldGenerator generator) {
		this(name, chunkSize, DEFAULT_CHUNK_DEPTH, generator);
	}
	
	protected StaticChunkWorld(final String name, final WorldGenerator generator) {
		this(name, DEFAULT_CHUNK_SIZE, generator);
	}
	
}
