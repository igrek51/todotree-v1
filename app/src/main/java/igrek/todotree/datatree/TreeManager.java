package igrek.todotree.datatree;

import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.exceptions.NoSuperItemException;

public class TreeManager {
	
	private TreeItem rootItem;
	private TreeItem currentItem;
	
	private Integer newItemPosition;
	
	public TreeManager() {
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
		currentItem.add(position, content);
	}
	
	public void addToCurrent(Integer position, TreeItem item) {
		if (position == null) {
			position = currentItem.size();
		}
		currentItem.add(position, item);
	}
	
	public void addToCurrent(TreeItem newItem) {
		currentItem.add(newItem);
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
