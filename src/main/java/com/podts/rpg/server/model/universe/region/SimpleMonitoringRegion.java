package com.podts.rpg.server.model.universe.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.podts.rpg.server.model.universe.Entity;

public abstract class SimpleMonitoringRegion extends SimpleRegion implements MonitoringRegion {
	
	private Map<Integer,Entity> entities;
	private Collection<Entity> safeEntities;

	@Override
	public final MonitoringRegion addEntity(Entity entity) {
		entities.put(entity.getID(), entity);
		return this;
	}

	@Override
	public final MonitoringRegion removeEntity(Entity entity) {
		entities.remove(entity.getID());
		return this;
	}

	@Override
	public final Collection<Entity> getEntities() {
		return safeEntities;
	}
	
	public SimpleMonitoringRegion() {
		this(new ArrayList<Entity>());
	}
	
	public SimpleMonitoringRegion(Collection<Entity> entities) {
		this.entities = new LinkedHashMap<>();
		for(Entity e : entities) {
			addEntity(e);
		}
		safeEntities = Collections.unmodifiableCollection(this.entities.values());
	}
	
}
