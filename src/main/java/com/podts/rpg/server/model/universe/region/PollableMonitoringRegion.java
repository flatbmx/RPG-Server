package com.podts.rpg.server.model.universe.region;

import java.util.stream.Stream;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.universe.Entity;

public interface PollableMonitoringRegion extends PollableRegion, MonitoringRegion {

	@Override
	default Stream<Player> players() {
		return MonitoringRegion.super.players();
	}

	@Override
	default Stream<Entity> entities() {
		return MonitoringRegion.super.entities();
	}
	
}
