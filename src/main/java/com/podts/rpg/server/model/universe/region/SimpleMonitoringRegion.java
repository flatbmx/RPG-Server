package com.podts.rpg.server.model.universe.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.podts.rpg.server.model.universe.Entity;

public abstract class SimpleMonitoringRegion extends SimpleRegion implements MonitoringRegion {
	
	private Collection<Entity> entities;
	private Collection<Entity> safeEntities;

	@Override
	public final MonitoringRegion addEntity(Entity entity) {
		entities.add(entity);
		return this;
	}

	@Override
	public final MonitoringRegion removeEntity(Entity entity) {
		entities.remove(entity);
		return this;
	}

	@Override
	public final Collection<Entity> getEntities() {
		return safeEntities;
	}
	
	public SimpleMonitoringRegion() {
		this(new ArrayList<Entity>());
	}
	
	public SimpleMonitoringRegion(List<Entity> entities) {
		this.entities = entities;
		safeEntities = Collections.unmodifiableCollection(entities);
	}
	
}
