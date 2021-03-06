package com.podts.rpg.server.model.universe;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.podts.rpg.server.model.universe.Location.MoveType;
import com.podts.rpg.server.model.universe.region.PollableRegion;

public class Spaces {
	
	private static class UnRegisterableSpace extends Space {
		
		private final Space space;
		
		@Override
		public Collection<? extends Plane> getPlanes() {
			return space.getPlanes();
		}
		
		@Override
		public Location createLocation(int x, int y, int z) {
			return space.createLocation(x, y, z);
		}

		@Override
		public boolean isRegistered(Registerable r) {
			return space.isRegistered(r);
		}

		@Override
		public boolean register(Registerable r) {
			return false;
		}

		@Override
		public boolean deRegister(Registerable r) {
			return false;
		}
		
		@Override
		public Stream<? extends Plane> planes() {
			return space.planes();
		}
		
		@Override
		public Stream<PollableRegion> regions() {
			return space.regions();
		}
		
		@Override
		public Stream<Entity> entities() {
			return space.entities();
		}

		@Override
		public
		Location moveEntity(Entity entity, MoveType update, int dx, int dy, int dz) {
			return space.moveEntity(entity, update, dx, dy, dz);
		}
		
		private UnRegisterableSpace(Space space) {
			this.space = space;
		}

		@Override
		public Optional<Tile> getTile(Location point) {
			return space.getTile(point);
		}

		@Override
		public Stream<Entity> nearbyEntities(HasLocation l) {
			return space.nearbyEntities(l);
		}

		@Override
		public Space moveEntity(Entity entity, Location newLocation, MoveType update) {
			return space.moveEntity(entity, newLocation, update);
		}
		
	}
	
	public static final Space unRegisterableSpace(Space space) {
		return new UnRegisterableSpace(space);
	}
	
}
