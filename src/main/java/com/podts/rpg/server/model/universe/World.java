package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;
import com.podts.rpg.server.model.universe.region.Region;
import com.podts.rpg.server.model.universe.region.RegionListener;
import com.podts.rpg.server.model.universe.region.SimpleRegionHandler;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.EntityPacket.UpdateType;
import com.podts.rpg.server.network.packet.TilePacket;

/**
 * A collection of Tiles that represent a world that entities such as NPCs, players, etc can inhabit and interact with
 * each other and the world it self.
 *
 */
public abstract class World extends SimpleRegionHandler implements Region {
	
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
	public abstract Tile getTile(Locatable point);
	
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
		
		doSetTile(newTile);
		
		TilePacket updatePacket = new TilePacket(newTile);
		sendToNearbyPlayers(newTile, updatePacket);
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
	
	protected abstract boolean doRegister(Entity e);
	
	/**
	 * Un-registers an {@link Entity} that is already {@link #register(Entity) registered} in this {@link Word}.
	 * If the Entity is null or is not registered in this world it will silently return.
	 * @param e - The {@link Entity} that will be un-registered.
	 * @return The World for chaining.
	 */
	public World deRegister(Entity e) {
		if(doDeRegister(e)) {
			sendToNearbyPlayers(e, EntityPacket.constructDestroy(e));
		}
		return this;
	}
	
	protected abstract boolean doDeRegister(Entity e);
	
	/**
	 * Registers the given {@link PollableRegion region} with this {@link World world}.
	 * {@link Region Regions} that are not registered will not have their
	 * {@link RegionListener#onEnter(Region, Entity, MoveType) onEnter} nor {@link RegionListener#onLeave(Region, Entity, MoveType) onLeave} methods called.
	 * If the {@link Region region} is already registered then it will follow any changes made to the Region.
	 * @param r - The given {@link PollableRegion region} to register.
	 * @return The {@link World world} for chaining.
	 */
	public abstract World registerRegion(PollableRegion r);
	
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
		doMoveEntity(entity, result, type);
		return result;
	}
	
	public abstract Location createLocation(int x, int y, int z);
	
	@Override
	public final boolean contains(Locatable loc) {
		if(loc == null) throw new IllegalArgumentException("Cannot determine if null Locatable is in World.");
		return equals(loc.getWorld());
	}
	
	@Override
	public String toString() {
		return "World - " + name;
	}
	
	private static final void fireRegionEnter(Region r, Entity e, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onEnter(r, e, type);
		}
	}
	
	private static final void fireRegionMove(Region r, Entity e, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onMove(r, e, type);
		}
	}
	
	private static final void fireRegionLeave(Region r, Entity e, Location newLocation, MoveType type) {
		for(RegionListener l : r.getRegionListeners()) {
			l.onLeave(r, e, type);
		}
	}
	
	private void sendToNearbyPlayers(Locatable l, Packet packet) {
		for(Player player : getNearbyPlayers(l)) {
			player.getStream().sendPacket(packet);
		}
	}
	
	protected World(String name, WorldGenerator generator) {
		this.name = name;
		this.generator = generator;
	}
	
}
