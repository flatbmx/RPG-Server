package com.podts.rpg.server.model.universe.region;

import java.util.Collection;

import com.podts.rpg.server.model.universe.Entity;

/**
 * Region that monitors when entities enter and leave the region such that it keeps a record
 * of all the entities inside.
 *
 */
public interface MonitoringRegion extends Region {
	
	public default MonitoringRegion addEntities(Iterable<Entity> entities) {
		for(Entity entity : entities) {
			addEntity(entity);
		}
		return this;
	}
	
	public MonitoringRegion addEntity(Entity entity);
	public MonitoringRegion removeEntity(Entity entity);
	
	public default MonitoringRegion removeEntities(Iterable<Entity> entities) {
		for(Entity entity : entities) {
			removeEntity(entity);
		}
		return this;
	}
	
	/**
	 * Returns a Collection containing all the entities inside this region.
	 * The order of the entities correspond to when the entities entered the region.
	 * @return A Collection containing all the entities inside the region.
	 */
	public Collection<Entity> getEntities();
	
}
