package com.podts.rpg.server.model.universe.path;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.podts.rpg.server.model.universe.Space;
import com.podts.rpg.server.model.universe.HasLocation;
import com.podts.rpg.server.model.universe.Tile;

public class ReferencePathFinder implements PathFinder {
	
	@Override
	public Path findPath(HasLocation start, HasLocation finish, PathDecider decider) {
		if(start == null || finish == null)
			return null;
		if(start.isNowhere() || finish.isNowhere())
			return null;
		
		Tile startTile = start.getTile();
		if(!startTile.isTraversable())
			return null;
		
		Tile finishTile = finish.getTile();
		if(!finishTile.isTraversable())
			return null;
		
		if(start.isAt(finish))
			return new GeneralListPath(start.getTile());
		
		Space space = startTile.getSpace();
		LinkedList<ReferencePath> paths = new OrderedList<ReferencePath>(decider);
		paths.addFirst(new ReferencePath(startTile));
		
		while(!paths.isEmpty()) {
			
			ReferencePath path = paths.pop();
			
			for(Tile newTile : space.getSurroundingTilesIterable(path.getEnd())) {
				if(!newTile.isTraversable() || path.contains(newTile))
					continue;
				
				ReferencePath newPath = new ReferencePath(path, newTile);
				
				if(decider.test(newPath)) {
					if(newTile.isAt(finish))
						return newPath.finalizePath();
					paths.add(newPath);
				}
					
			}
			
		}
		
		return null;
	}
	
	private class OrderedList<T> extends LinkedList<T> {
		
		private static final long serialVersionUID = 7752541094933228489L;
		private final Comparator<? super T> comparator;
		
		@Override
	    public boolean add(T element) {
	        ListIterator<T> itr = listIterator();
	        while(true) {
	            if (!itr.hasNext()) {
	                itr.add(element);
	                return true;
	            }

	            T elementInList = itr.next();
	            if (comparator.compare(element, elementInList) > 0) {
	                itr.previous();
	                itr.add(element);
	                return true;
	            }
	        }
	    }
		
		OrderedList(Comparator<? super T> comparator) {
			this.comparator = comparator;
		}
		
	}
	
}
