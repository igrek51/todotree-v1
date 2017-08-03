package igrek.todotree.services.history.change;


import igrek.todotree.model.treeitem.AbstractTreeItem;

public class RemoveItemItemChange extends AbstractItemChange {
	
	private AbstractTreeItem removedItem;
	private AbstractTreeItem parent;
	
	public RemoveItemItemChange(AbstractTreeItem removedItem, AbstractTreeItem parent) {
		this.removedItem = removedItem;
		this.parent = parent;
	}
	
	public AbstractTreeItem getRemovedItem() {
		return removedItem;
	}
	
	public AbstractTreeItem getParent() {
		return parent;
	}
	
	@Override
	public void revert() {
		//TODO
	}
}
