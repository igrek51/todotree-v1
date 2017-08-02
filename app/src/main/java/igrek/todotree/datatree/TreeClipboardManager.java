package igrek.todotree.datatree;


import java.util.ArrayList;
import java.util.List;

import igrek.todotree.datatree.item.TreeItem;

public class TreeClipboardManager {
	
	private List<TreeItem> clipboard = null;
	
	public List<TreeItem> getClipboard() {
		return clipboard;
	}
	
	public int getClipboardSize() {
		if (clipboard == null)
			return 0;
		return clipboard.size();
	}
	
	public boolean isClipboardEmpty() {
		return clipboard == null || clipboard.size() == 0;
	}
	
	public void clearClipboard() {
		clipboard = null;
	}
	
	public void addToClipboard(TreeItem item) {
		if (clipboard == null) {
			clipboard = new ArrayList<>();
		}
		clipboard.add(new TreeItem(item));
	}
	
	public void recopyClipboard() {
		if (clipboard != null) {
			ArrayList<TreeItem> newClipboard = new ArrayList<>();
			for (TreeItem item : clipboard) {
				newClipboard.add(new TreeItem(item));
			}
			clipboard = newClipboard;
		}
	}
}
