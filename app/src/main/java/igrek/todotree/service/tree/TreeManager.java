package igrek.todotree.service.tree;

import java.util.TreeSet;

import igrek.todotree.commands.StatisticsCommand;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.RootTreeItem;
import igrek.todotree.exceptions.NoSuperItemException;
import igrek.todotree.service.history.ChangesHistory;

public class TreeManager {
	
	private AbstractTreeItem rootItem;
	private AbstractTreeItem currentItem;
	
	private Integer newItemPosition;
	
	private ChangesHistory changesHistory;
	
	public TreeManager(ChangesHistory changesHistory) {
		this.changesHistory = changesHistory;
		reset();
	}
	
	public void reset() {
		rootItem = new RootTreeItem();
		currentItem = rootItem;
	}
	
	public AbstractTreeItem getRootItem() {
		return rootItem;
	}
	
	public void setRootItem(AbstractTreeItem rootItem) {
		this.rootItem = rootItem;
		this.currentItem = rootItem;
	}
	
	public AbstractTreeItem getCurrentItem() {
		return currentItem;
	}
	
	public boolean isPositionBeyond(int position) {
		return position >= currentItem.size();
	}
	
	public boolean isPositionAtItem(int position) {
		return position >= 0 && position < currentItem.size();
	}
	
	public int positionAfterEnd() {
		return currentItem.size();
	}
	
	public TreeSet<Integer> getAllChildrenIds() {
		TreeSet<Integer> ids = new TreeSet<>();
		for (int id = 0; id < currentItem.getChildren().size(); id++) {
			ids.add(id);
		}
		return ids;
	}
	
	public void setNewItemPosition(Integer newItemPosition) {
		this.newItemPosition = newItemPosition;
	}
	
	public Integer getNewItemPosition() {
		return newItemPosition;
	}
	
	public AbstractTreeItem getChild(int position) {
		return currentItem.getChild(position);
	}
	
	public void addToCurrent(Integer position, AbstractTreeItem item) {
		if (position == null) {
			position = currentItem.size();
		}
		item.setParent(currentItem);
		changesHistory.registerChange();
		currentItem.add(position, item);
		new StatisticsCommand().onTaskCreated(item);
	}
	
	public void removeFromCurrent(int position) {
		AbstractTreeItem removingChild = currentItem.getChild(position);
		new StatisticsCommand().onTaskRemoved(removingChild);
		changesHistory.registerChange();
		currentItem.remove(position);
	}
	
	public void removeFromCurrent(AbstractTreeItem item) {
		changesHistory.registerChange();
		new StatisticsCommand().onTaskRemoved(item);
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
		AbstractTreeItem item = currentItem.getChild(childIndex);
		goTo(item);
	}
	
	public void goTo(AbstractTreeItem child) {
		currentItem = child;
	}
	
}
