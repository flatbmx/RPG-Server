package com.podts.rpg.server.model.universe.region;

import com.podts.rpg.server.model.universe.HasLocation;

public interface SetRegion extends PollableRegion {
	
	public SetRegion addPoint(HasLocation point);
	public SetRegion removePoint(HasLocation point);
	
}
