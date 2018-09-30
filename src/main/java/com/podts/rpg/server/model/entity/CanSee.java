package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.universe.Locatable;

public interface CanSee extends Locatable {
	
	public double getViewingDistance();
	
}
