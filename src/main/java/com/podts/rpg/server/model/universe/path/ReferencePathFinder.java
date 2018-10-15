package com.podts.rpg.server.model.universe.path;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

import com.podts.rpg.server.model.universe.HasLocation;
import com.podts.rpg.server.model.universe.Space;
import com.podts.rpg.server.model.universe.Tile;

public class ReferencePathFinder implements PathFinder {
	
	@Override
	public Optional<Path> findPath(HasLocation start, HasLocation finish, PathDecider decider) {
		if(start == null || finish == null)
			return Optional.empty();
		if(start.isNowhere() || finish.isNowhere())
			return Optional.empty();
		
		if(!start.isInPlane(finish))
			return Optional.empty();
		
		Tile startTile = start.getTile();
		if(!startTile.isTraversable())
			return Optional.empty();
		
		Tile finishTile = finish.getTile();
		if(!finishTile.isTraversable())
			return Optional.empty();
		
		if(start.isAt(finish))
			return Optional.of(new GeneralListPath(start.getTile()));
		
		Space space = startTile.getSpace();
		Queue<ReferencePath> paths = new PriorityQueue<ReferencePath>(decider);
		paths.add(new ReferencePath(startTile));
		
		while(!paths.isEmpty()) {
			
			ReferencePath path = paths.poll();
			
			for(Tile newTile : space.getSurroundingTilesIterable(path.getEnd())) {
				if(!newTile.isTraversable() || path.contains(newTile))
					continue;
				
				ReferencePath newPath = new ReferencePath(path, newTile);
				
				if(decider.test(newPath)) {
					if(newTile.isAt(finish))
						return Optional.of(newPath.finalizePath());
					paths.add(newPath);
				}
					
			}
			
		}
		
		return Optional.empty();
	}
	
}
