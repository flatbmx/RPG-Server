package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.Utils;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Location.Direction;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.region.MonitoringRegion;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.RegionListener;
import com.podts.rpg.server.model.universe.region.SimpleRegion;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.TilePacket;

/**
 * A collection of Tiles that represent a world that entities such as NPCs, players, etc can inhabit and interact with
 * each other and the world it self.
 *
 */
public abstract class World extends SimpleRegion {
	
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
		Utils.assertNullArg(loc, "Cannot get Tile for null location.");
		Utils.assertArg(!doContains(loc), "Cannot get Tile that exists in a different World.");
		return doGetTile(loc.getLocation());
	}
	
	protected abstract Tile doGetTile(Location point);
	
	public final World getTiles(Tile[][] tiles, Location topLeft) {
		Utils.assertNullArg(tiles, "Cannot get Tiles with null array.");
		Utils.assertNullArg(topLeft, "Cannot get Tiles with null starting point.");
		Utils.assertArg(tiles.length == 0 || tiles[0].length == 0, "Cannot get Tiles with array length of 0.");
		Utils.assertArg(!doContains(topLeft), "Cannot get Tiles with starting point from a different world.");
		
		doGetTiles(tiles, topLeft);
		return this;
	}
	
	protected abstract void doGetTiles(Tile[][] tiles, Location topLeft);
	
	/**
	 * Set the the given Tile at the given point in this World.
	 * @param newTile - The new Tile.
	 * @param point - The location of the new Tile.
	 * @return The World for chaining.
	 */
	public final World setTile(Tile newTile) {
		Utils.assertNullArg(newTile, "Cannot set a Tile as null.");
		Utils.assertArg(!doContains(newTile), "Cannot set a Tile that exists in another World.");
		
		doSetTile(newTile);
		
		sendToNearbyPlayers(newTile, TilePacket.constructCreate(newTile));
		return this;
	}
	
	public final World setTiles(Collection<Tile> tiles) {
		Utils.assertNullArg(tiles, "Cannot set Tiles as null.");
		if(tiles.isEmpty()) return this;
		
		for(Tile t : tiles) {
			if(t == null) continue;
			if(!doContains(t)) continue;
			doSetTile(t);
			sendToNearbyPlayers(t, TilePacket.constructCreate(t));
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
	public final Collection<Entity> getNearbyEntities(Locatable l, double distance, Predicate<Entity> condition) {
		Utils.assertNull(l, "Cannot find surrounding Entities from null location.");
		Utils.assertArg(!doContains(l), "Cannot find surrounding Entities from location not in this world.");
		return doGetNearbyEntities(l.getLocation(), distance, condition);
	}
	
	public final Collection<Entity> getNearbyEntities(Locatable l, double distance) {
		return getNearbyEntities(l, distance, null);
	}
	
	public abstract Collection<Entity> doGetNearbyEntities(Location point, double distance, Predicate<Entity> condition);
	
	/**
	 * Returns all nearby registered entities relative to a given point in this World.
	 * All entities are in the same Z plane as the given point.
	 * It is up to the Worlds decision as how "nearby" is defined.
	 * Different World implementations may have different ranges based on their native backings.
	 * If you wish to get all nearby entities within a certain range then use {@link #getNearbyEntities(Location,double) getNearbyEntities}
	 * The {@link Collection} may be modifiable or not however any modifications will <b>NOT</b> represent any World changes.
	 * @param l - The central point of all entities.
	 * @param condition - A condition that all returned entities meet.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public final Collection<Entity> getNearbyEntities(Locatable l, Predicate<Entity> condition) {
		Utils.assertNull(l, "Cannot find nearby entities from null locatable.");
		Utils.assertNull(l.getLocation(), "Cannot find nearby entities from null location.");
		Utils.assertArg(doContains(l), "Cannot find nearby entities from location in another world.");
		return doGetNearbyEntities(l.getLocation(), condition);
	}
	
	/**
	 * This method is equivalent to calling {@link #getNearbyEntities(Locatable,Predicate) getNearbyEntities} with no condition.
	 * @param l - The central point of all entities.
	 * @return A Collection of entities within the given radius in this World.
	 */
	public final Collection<Entity> getNearbyEntities(Locatable l) {
		return getNearbyEntities(l, null);
	}
	
	public abstract Collection<Entity> doGetNearbyEntities(Location point, Predicate<Entity> condition);
	
	public final Collection<Player> getNearbyPlayers(Locatable l) {
		Utils.assertNull(l, "Cannot find nearby players from null locatable.");
		Utils.assertNull(l.getLocation(), "Cannot find nearby players from null location.");
		Utils.assertArg(doContains(l), "Cannot find nearby players from location in another world.");
		return doGetNearbyPlayers(l.getLocation());
	}
	
	public abstract Collection<Player> doGetNearbyPlayers(Location point);
	
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
	public final boolean register(Entity e) {
		boolean result = doRegister(e);
		if(result) {
			if(e instanceof PlayerEntity)
				sendToNearbyPlayers(e, ((PlayerEntity)e).getPlayer(), EntityPacket.constructCreate(e));
			else
				sendToNearbyPlayers(e, EntityPacket.constructCreate(e));
		}
			
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
		Utils.assertNull(region, "Cannot register a null region!");
		
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
		
		Set<Region> oldRegions = new HashSet<>(getRegionsAtLocation(entity.getLocation()));
		Set<Region> staleRegions = new HashSet<>();
		Set<Region> newRegions = new HashSet<>(getRegionsAtLocation(newLoc));
		
		Iterator<Region> it = newRegions.iterator();
		while(it.hasNext()) {
			Region r = it.next();
			if(oldRegions.contains(r)) {
				staleRegions.add(r);
				oldRegions.remove(r);
				it.remove();
			}
		}
		
		for(Region r : oldRegions) {
			//Old regions
			fireRegionLeave(r, entity, newLoc, type);
		}
		for(Region r : staleRegions) {
			//Stale regions
			fireRegionMove(r, entity, newLoc, type);
		}
		for(Region r : newRegions) {
			//New regions
			fireRegionEnter(r, entity, newLoc, type);
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
		Utils.assertNullArg(type, "Cannot create Tile with null type!");
		Utils.assertNullArg(location, "Cannot create Tile with null location!");
		Utils.assertArg(!doContains(location), "Cannot create Tile with location in a different world.");
		
		return new Tile(type, location);
	}
	
	public final Tile createTile(TileType type, int x, int y, int z) {
		Utils.assertNullArg(type, "Cannot create Tile with null type!");
		return new Tile(type, createLocation(x,y,z));
	}
	
	@Override
	public final boolean contains(Locatable loc) {
		Utils.assertNullArg(loc, "Cannot determine if null Locatable is in World.");
		return doContains(loc);
	}
	
	protected final boolean doContains(Locatable loc) {
		return equals(loc.getWorld());
	}
	
	@Override
	public String toString() {
		return "World - " + name;
	}
	
	private static final void fireRegionEnter(Region r, Entity entity, Location newLocation, MoveType type) {
		if(r instanceof MonitoringRegion) {
			((MonitoringRegion) r).addEntity(entity);
		}
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityEnter(r, entity, type);
		}
	}
	
	private static final void fireRegionMove(Region r, Entity entity, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityMove(r, entity, type);
		}
	}
	
	private static final void fireRegionLeave(Region r, Entity entity, Location newLocation, MoveType type) {
		if(r instanceof MonitoringRegion) {
			((MonitoringRegion) r).removeEntity(entity);
		}
		for(RegionListener l : r.getRegionListeners()) {
			l.onEntityLeave(r, entity, type);
		}
	}
	
	protected final void sendToNearbyPlayers(Locatable l, Packet... packets) {
		for(Player player : getNearbyPlayers(l)) {
			for(Packet p : packets)
				player.sendPacket(p);
		}
	}
	
	protected final void sendToNearbyPlayers(Locatable l, Player except, Packet... packets) {
		for(Player player : getNearbyPlayers(l)) {
			if(player.equals(except)) continue;
			for(Packet p : packets)
				player.sendPacket(p);
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
