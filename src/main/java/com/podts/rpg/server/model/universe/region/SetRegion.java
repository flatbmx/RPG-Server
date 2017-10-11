package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Locatable;

public interface SetRegion extends PollableRegion {
	
	public SetRegion addPoint(Locatable loc);
	public SetRegion removePoint(Locatable loc);
	
}
