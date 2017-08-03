package igrek.todotree.services.tree;


import java.util.HashMap;

import igrek.todotree.model.treeitem.AbstractTreeItem;

public class TreeScrollCache {
	
	private HashMap<AbstractTreeItem, Integer> storedScrollPositions;
	
	public TreeScrollCache() {
		storedScrollPositions = new HashMap<>();
	}
	
	public void storeScrollPosition(AbstractTreeItem item, Integer y) {
		if (item != null && y != null) {
			storedScrollPositions.put(item, y);
		}
	}
	
	public Integer restoreScrollPosition(AbstractTreeItem item) {
		return storedScrollPositions.get(item);
	}
	
	public void clear() {
		storedScrollPositions.clear();
	}
}
