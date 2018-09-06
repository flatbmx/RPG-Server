package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.Spatial;

public interface SetRegion extends PollableRegion {
	
	public SetRegion addPoint(Spatial point);
	public SetRegion removePoint(Spatial point);
	
}
