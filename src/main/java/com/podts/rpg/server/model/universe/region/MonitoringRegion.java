package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Entity;

public interface MonitoringRegion extends Region {
	
	public default MonitoringRegion addEntities(Iterable<Entity> entities) {
		for(Entity entity : entities) {
			addEntity(entity);
		}
		return this;
	}
	
	public MonitoringRegion addEntity(Entity entity);
	
}
