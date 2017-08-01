package igrek.todotree.services.datatree;


import java.util.HashMap;

public class TreeScrollStore {
	
	private HashMap<TreeItem, Integer> storedScrollPositions;
	
	TreeScrollStore() {
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
