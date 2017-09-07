package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.region.MonitoringRegion;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.RegionListener;
import com.podts.rpg.server.model.universe.region.SimpleRegionHandler;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.TilePacket.TileUpdateType;
import com.podts.rpg.server.network.packet.TilePacket;

/**
 * A collection of Tiles that represent a world that entities such as NPCs, players, etc can inhabit and interact with
 * each other and the world it self.
 *
 */
public abstract class World extends SimpleRegionHandler {
	
	private final WorldGenerator generator;
	private String name;
	
	/**
	 * Returns the name of the World.
	 * @return The Worlds name.
	 */
	public final String getName() {
		return name;
	}
	
	protected final void setName(String newName) {
		name = newName;
	}
	
	/**
	 * Returns the Worlds {@link WorldGenerator}.
	 * @return
	 */
	public final WorldGenerator getWorldGenerator() {
		return generator;
	}
	
	public abstract Collection<Player> getPlayers();
	
	/**
	 * Returns the tile that is located at the given point in this World.
	 * @param point - The location of the Tile.
	 * @return The Tile that is located at the given point.
	 */
	public final Tile getTile(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot get Tile for null location.");
		if(!equals(loc.getLocation().getWorld())) throw new IllegalArgumentException("Cannot get Tile that exists in a different World.");
		return doGetTile(loc.getLocation());
	}
	
	protected abstract Tile doGetTile(Location point);
	
	public final World getTiles(Tile[][] tiles, Location topLeft) {
		if(tiles == null) throw new IllegalArgumentException("Cannot get Tiles with null array.");
		if(topLeft == null) throw new IllegalArgumentException("Cannot get Tiles with null starting point.");
		if(tiles.length == 0 || tiles[0].length == 0) throw new IllegalArgumentException("Cannot get Tiles with array length of 0.");
		if(!doContains(topLeft)) throw new IllegalArgumentException("Cannot get Tiles with starting point from a different world.");
		return doGetTiles(tiles, topLeft);
	}
	
	protected abstract World doGetTiles(Tile[][] tiles, Location topLeft);
	
	/**
	 * Set the the given Tile at the given point in this World.
	 * @param newTile - The new Tile.
	 * @param point - The location of the new Tile.
	 * @return The World for chaining.
	 */
	public final World setTile(Tile newTile) {
		if(newTile == null) throw new IllegalArgumentException("Cannot set a Tile as null.");
		Location point = newTile.getLocation();
		if(point == null) throw new IllegalArgumentException("Cannot set a Tile at a null location.");
		if(!equals(point.getWorld())) throw new IllegalArgumentException("Cannot set a Tile that exists in another World.");
		
		final Tile oldTile = doGetTile(point);
		
		
		
		doSetTile(newTile);
		
		TilePacket updatePacket = new TilePacket(newTile, TileUpdateType.CREATE);
		sendToNearbyPlayers(newTile, updatePacket);
		return this;
	}
	
	public final World setTiles(Collection<Tile> tiles) {
		if(tiles == null) throw new IllegalArgumentException("Cannot set Tiles as null.");
		if(tiles.isEmpty()) return this;
		
		for(Tile t : tiles) {
			if(t == null) continue;
			if(!doContains(t)) continue;
			doSetTile(t);
			sendToNearbyPlayers(t, new TilePacket(t, TileUpdateType.CREATE));
		}
		
		return this;
	}
	
	/**
	 * This method saves this new Tile and nothing else.
	 * @param newTile - The new tile that should be in the world.
	 */
	protected abstract void doSetTile(Tile newTile);
	
	/**
	 * Returns all registered entities that are within a given distance around a given point in this World.
	 * All entities are in the same Z plane as the given point.
	 * The {@link Collection} may be modifiable or not however any modifications will <b>NOT</b> represent any World changes.
	 * @param l - The central point
	 * @param distance - The radius of entities around the given point.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public abstract Collection<Entity> getNearbyEntities(Locatable l, double distance);
	
	/**
	 * Returns all nearby registered entities relative to a given point in this World.
	 * All entities are in the same Z plane as the given point.
	 * It is up to the Worlds decision as how "nearby" is defined.
	 * Different World implementations will have different ranges based on their native backings.
	 * The {@link Collection} may be modifiable or not however any modifications will <b>NOT</b> represent any World changes.
	 * @param l - The central point of all entities.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public abstract Collection<Entity> getNearbyEntities(Locatable l);
	
	public abstract Collection<Player> getNearbyPlayers(Locatable l);
	
	/**
	 * Returns the {@link #register(Entity) registered} {@link Entity} with the given id.
	 * @param id - The ID of the desired {@link Entity}.
	 * @return The {@link Entity} that has the given ID otherwise null if the {@link Entity} either does not exist or is not {@link #register(Entity) registered}.
	 */
	public abstract Entity getEntity(int id);
	
	/**
	 * Registers an entity with this {@link Word}.
	 * Any non-registered entities will not be viewable by {@link #getEntity(int)},
	 * {@link #getNearbyEntities(Locatable)} nor {@link #getNearbyEntities(Locatable,double)}.
	 * @param e - The {@link Entity} that is to be registered.
	 * @return True if the {@link Entity} was sucessfully registered, false if there already exists an 
	 * {@link Entity} that has the same ID in this {@link Word}.
	 */
	public boolean register(Entity e) {
		boolean result = doRegister(e);
		if(result)
			sendToNearbyPlayers(e, EntityPacket.constructCreate(e));
		return result;
	}
	
	public boolean register(PlayerEntity e) {
		boolean result = doRegister(e);
		if(result)
			sendToNearbyPlayers(e, e.getPlayer(), EntityPacket.constructCreate(e));
		return result;
	}
	
	protected abstract boolean doRegister(Entity e);
	
	/**
	 * Un-registers an {@link Entity} that is already {@link #register(Entity) registered} in this {@link Word}.
	 * If the Entity is null or is not registered in this world it will silently return.
	 * @param e - The {@link Entity} that will be un-registered.
	 * @return The World for chaining.
	 */
	public final World deRegister(Entity e) {
		if(doDeRegister(e)) {
			sendToNearbyPlayers(e, EntityPacket.constructDestroy(e));
		}
		return this;
	}
	
	protected abstract boolean doDeRegister(Entity e);
	
	/**
	 * Registers the given {@link PollableRegion region} with this {@link World world}.
	 * {@link Region Regions} that are not registered will not have their
	 * {@link RegionListener#onEntityEnter(Region, Entity, MoveType) onEnter} nor {@link RegionListener#onEntityLeave(Region, Entity, MoveType) onLeave} methods called.
	 * If the {@link Region region} is already registered then it will follow any changes made to the Region.
	 * @param region - The given {@link PollableRegion region} to register.
	 * @return The {@link World world} for chaining.
	 */
	public final World registerRegion(PollableRegion region) {
		if(region == null) throw new NullPointerException("Cannot register a null region!");
		doRegisterRegion(region);
		if(region instanceof MonitoringRegion) {
			MonitoringRegion mR = (MonitoringRegion) region;
			mR.addEntities(getEntitiesInRegion(region));
		}
		return this;
	}
	
	protected abstract void doRegisterRegion(PollableRegion r);
	
	/**
	 * Un-registers the given {@link PollableRegion region} from this World.
	 * If the {@link PollableRegion region} was not previously {@link #registerRegion(PollableRegion) registered} then nothing will happen.
	 * @param r - The given Region to un-register.
	 * @return The World for chaining.
	 */
	public abstract World deRegisterRegion(PollableRegion r);
	
	/**
	 * Returns all registered {@link PollableRegion}s that contain a given {@link Location}.
	 * @param loc - The given point.
	 * @return A {@link Collection} of all the registered {@link PollableRegion}s that {@link Region#contains(Locatable) contains} the given point.
	 * The Collection may be modifiable or not however any changes will not affect anything outside of the Collection returned.
	 */
	public abstract Collection<Region> getRegionsAtLocation(Locatable loc);
	
	/**
	 * Returns all {@link Entity entities} that are within the given {@link PollableRegion region}.
	 * The {@link PollableRegion region} does <b>NOT</b> have to be {@link #registerRegion(Region) registered} for this method to operate.
	 * @param r - The given {@link PollableRegion} that all entities are in.
	 * @return A {@link Collection} that contains all entities within this {@link Region}. The Collection may be modifable or not
	 * however any modifications will not affect the Entities, Region or World in any way.
	 */
	public abstract Collection<Entity> getEntitiesInRegion(PollableRegion r);
	
	protected final World moveEntity(Entity entity, Location newLoc, MoveType type) {
		Location currentLoc = entity.getLocation();
		
		Set<Region> oldRegions = new HashSet<>(getRegionsAtLocation(currentLoc));
		for(Region r : getRegionsAtLocation(newLoc)) {
			if(!oldRegions.contains(r)) {
				//New region
				fireRegionEnter(r, entity, newLoc, type);
			} else {
				//Move
				fireRegionMove(r, entity, newLoc, type);
				oldRegions.remove(r);
			}
		}
		for(Region r : oldRegions) {
			//Old regions
			fireRegionLeave(r, entity, newLoc, type);
		}
		
		doMoveEntity(entity, newLoc, type);
		
		sendToNearbyPlayers(entity, EntityPacket.constructUpdate(entity));
		
		return this;
	}
	
	protected abstract World doMoveEntity(Entity entity, Location newLocation, MoveType type);
	
	protected final Location moveEntity(Entity entity, MoveType type, int dx, int dy, int dz) {
		Location result = entity.getLocation().move(dx, dy, dz);
		moveEntity(entity, result, type);
		return result;
	}
	
	public abstract Location createLocation(int x, int y, int z);
	
	public final Tile createTile(TileType type, Location location) {
		if(type == null) throw new IllegalArgumentException("Cannot create Tile with null type!");
		if(location == null) throw new IllegalArgumentException("Cannot create Tile with null location!");
		if(!this.equals(location.getWorld())) throw new IllegalArgumentException("Cannot create Tile with location in a different world.");
		return new Tile(type, location);
	}
	
	@Override
	public final boolean contains(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot determine if null Locatable is in World.");
		return doContains(loc);
	}
	
	protected final boolean doContains(Locatable loc) {
		return equals(loc.getWorld());
	}
	
	@Override
	public String toString() {
		return "World - " + name;
	}
	
	private final void fireRegionEnter(Entity e, Location newLocation, MoveType type) {
		Set<Region> regions = new HashSet<>(getRegionsAtLocation(e.getLocation()));
		for(Region r : regions) {
			fireRegionEnter(r, e, newLocation, type);
		}
	}
	
	private static final void fireRegionEnter(Region r, Entity e, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityEnter(r, e, type);
		}
	}
	
	private final void fireRegionMove(Entity e, Location newLocation, MoveType type) {
		Set<Region> regions = new HashSet<>(getRegionsAtLocation(e.getLocation()));
		for(Region r : regions) {
			fireRegionMove(r, e, newLocation, type);
		}
	}
	
	private static final void fireRegionMove(Region r, Entity e, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityMove(r, e, type);
		}
	}
	
	private final void fireRegionLeave(Entity e, Location newLocation, MoveType type) {
		Set<Region> regions = new HashSet<>(getRegionsAtLocation(e.getLocation()));
		for(Region r : regions) {
			fireRegionLeave(r, e, newLocation, type);
		}
	}
	
	private static final void fireRegionLeave(Region r, Entity e, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityLeave(r, e, type);
		}
	}
	
	private final void fireRegionTileChange(Tile oldTile, Tile newTile) {
		Set<Region> regions = new HashSet<>(getRegionsAtLocation(oldTile));
		for(Region r : regions) {
			fireRegionTileChange(r, oldTile, newTile);
		}
	}
	
	private static final void fireRegionTileChange(Region r, Tile oldTile, Tile newTile) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onTileChange(r, oldTile, newTile);
		}
	}
	
	protected final void sendToNearbyPlayers(Locatable l, Packet... packets) {
		for(Player player : getNearbyPlayers(l)) {
			for(Packet p : packets)
				player.getStream().sendPacket(p);
		}
	}
	
	protected final void sendToNearbyPlayers(Locatable l, Player except, Packet... packets) {
		for(Player player : getNearbyPlayers(l)) {
			if(player.equals(except)) continue;
			for(Packet p : packets)
				player.getStream().sendPacket(p);
		}
	}
	
	protected World(String name, WorldGenerator generator) {
		this.name = name;
		this.generator = generator;
	}

	public final Location moveEntity(Entity entity, Direction dir) {
		return moveEntity(entity, MoveType.UPDATE, dir.getX(), dir.getY(), 0);
	}
	
	protected final void sendCreateEntity(Player player, Entity entity) {
		player.sendPacket(EntityPacket.constructCreate(entity));
	}
	
	protected final void sendUpdateEntity(Player player, Entity entity, Location newLocation) {
		player.sendPacket(EntityPacket.constructMove(entity, newLocation));
	}
	
	protected final void sendUpdateEntity(Player player, Entity entity) {
		sendUpdateEntity(player, entity, entity.getLocation());
	}
	
	protected final void sendDestroyEntity(Player player, Entity entity) {
		player.sendPacket(EntityPacket.constructDestroy(entity));
	}
	
}
