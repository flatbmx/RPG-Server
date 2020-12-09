package com.podts.rpg.server.model.universe.generators;

import com.podts.rpg.server.model.universe.Locatable;
import com.podts.rpg.server.model.universe.structure.Building;

public abstract class BuildingGenerator<B extends Building> extends StructureGenerator {
	
	public abstract B buildBuilding(Locatable center);
	
}
