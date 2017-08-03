package igrek.todotree.services.history.change;


import igrek.todotree.model.treeitem.TreeItem;

public class RemoveItemItemChange extends AbstractItemChange {
	
	private TreeItem removedItem;
	private TreeItem parent;
	
	public RemoveItemItemChange(TreeItem removedItem, TreeItem parent) {
		this.removedItem = removedItem;
		this.parent = parent;
	}
	
	public TreeItem getRemovedItem() {
		return removedItem;
	}
	
	public TreeItem getParent() {
		return parent;
	}
	
	@Override
	public void revert() {
		//TODO
	}
}
