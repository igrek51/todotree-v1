package igrek.todotree.datatree;


import java.util.HashMap;

import igrek.todotree.datatree.item.TreeItem;

public class TreeScrollCache {
	
	private HashMap<TreeItem, Integer> storedScrollPositions;
	
	TreeScrollCache() {
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
	
}
