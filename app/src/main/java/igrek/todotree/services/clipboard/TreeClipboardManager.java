package igrek.todotree.services.clipboard;


import java.util.ArrayList;
import java.util.List;

import igrek.todotree.model.treeitem.AbstractTreeItem;

public class TreeClipboardManager {
	
	private List<AbstractTreeItem> clipboard = null;
	private AbstractTreeItem copiedFrom = null;
	
	public List<AbstractTreeItem> getClipboard() {
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
	
	public void addToClipboard(AbstractTreeItem item) {
		if (clipboard == null) {
			clipboard = new ArrayList<>();
		}
		clipboard.add(item.clone());
	}
	
	public void recopyClipboard() {
		if (clipboard != null) {
			ArrayList<AbstractTreeItem> newClipboard = new ArrayList<>();
			for (AbstractTreeItem item : clipboard) {
				newClipboard.add(item.clone());
			}
			clipboard = newClipboard;
		}
	}
	
	public AbstractTreeItem getCopiedFrom() {
		return copiedFrom;
	}
	
	public void setCopiedFrom(AbstractTreeItem copiedFrom) {
		this.copiedFrom = copiedFrom;
	}
}
