package com.podts.rpg.server.model.universe;

import java.util.Collection;

import com.podts.rpg.server.model.Locatable;
import com.podts.rpg.server.model.entity.Entity;

public class StaticChunkWorld extends World {

	@Override
	public Tile getTile(int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setTile(Tile newTile, int x, int y, int z) {
		// TODO Auto-generated method stub
	}
	
	public Collection<Entity> getNearbyEntities(Locatable l, double distance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean register(Entity e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected StaticChunkWorld(String name) {
		super(name);
	}
	
}
