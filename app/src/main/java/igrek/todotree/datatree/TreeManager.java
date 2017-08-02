package igrek.todotree.datatree;

import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.services.history.ChangesHistory;

public class TreeManager {
	
	private TreeItem rootItem;
	private TreeItem currentItem;
	
	private Integer newItemPosition;
	
	private ChangesHistory changesHistory;
	
	public TreeManager(ChangesHistory changesHistory) {
		this.changesHistory = changesHistory;
		reset();
	}
	
	public void reset() {
		rootItem = new TreeItem(null, "/");
		currentItem = rootItem;
	}
	
	public TreeItem getRootItem() {
		return rootItem;
	}
	
	public void setRootItem(TreeItem rootItem) {
		this.rootItem = rootItem;
		this.currentItem = rootItem;
	}
	
	public TreeItem getCurrentItem() {
		return currentItem;
	}
	
	public boolean isPositionBeyond(int position) {
		return position >= currentItem.size();
	}
	
	public void setNewItemPosition(Integer newItemPosition) {
		this.newItemPosition = newItemPosition;
	}
	
	public Integer getNewItemPosition() {
		return newItemPosition;
	}
	
	public void addToCurrent(Integer position, String content) {
		if (position == null) {
			position = currentItem.size();
		}
		changesHistory.registerChange();
		currentItem.add(position, content);
	}
	
	public void addToCurrent(Integer position, TreeItem item) {
		if (position == null) {
			position = currentItem.size();
		}
		changesHistory.registerChange();
		currentItem.add(position, item);
	}
	
	public void addToCurrent(TreeItem newItem) {
		changesHistory.registerChange();
		currentItem.add(newItem);
	}
	
	public void addToCurrent(String content) {
		changesHistory.registerChange();
		currentItem.add(content);
	}
	
	public void removeFromCurrent(int position) {
		changesHistory.registerChange();
		currentItem.remove(position);
	}
	
	public void removeFromCurrent(TreeItem item) {
		changesHistory.registerChange();
		currentItem.remove(item);
	}
	
	//  Navigation
	
	public void goUp() throws NoSuperItemException {
		if (currentItem == rootItem) {
			throw new NoSuperItemException();
		} else if (currentItem.getParent() == null) {
			throw new IllegalStateException("parent = null. This should not happen");
		} else {
			currentItem = currentItem.getParent();
		}
	}
	
	public void goInto(int childIndex) {
		TreeItem item = currentItem.getChild(childIndex);
		goTo(item);
	}
	
	private void goTo(TreeItem child) {
		currentItem = child;
	}
	
}
