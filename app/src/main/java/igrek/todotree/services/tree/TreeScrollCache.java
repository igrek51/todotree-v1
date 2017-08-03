package igrek.todotree.services.tree;


import java.util.HashMap;

import igrek.todotree.model.treeitem.TreeItem;

public class TreeScrollCache {
	
	private HashMap<TreeItem, Integer> storedScrollPositions;
	
	public TreeScrollCache() {
		storedScrollPositions = new HashMap<>();
	}
	
	public void storeScrollPosition(TreeItem item, Integer y) {
		if (item != null && y != null) {
			storedScrollPositions.put(item, y);
		}
	}
	
	public Integer restoreScrollPosition(TreeItem item) {
		return storedScrollPositions.get(item);
	}
	
	public void clear() {
		storedScrollPositions.clear();
	}
}
