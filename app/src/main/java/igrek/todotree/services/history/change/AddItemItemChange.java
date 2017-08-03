package igrek.todotree.services.history.change;


import igrek.todotree.model.treeitem.AbstractTreeItem;

public class AddItemItemChange extends AbstractItemChange {
	
	private AbstractTreeItem newItem;
	private AbstractTreeItem parent;
	private int insertPosition;
	
	public AddItemItemChange(AbstractTreeItem newItem, AbstractTreeItem parent, int insertPosition) {
		this.newItem = newItem;
		this.parent = parent;
		this.insertPosition = insertPosition;
	}
	
	public AbstractTreeItem getNewItem() {
		return newItem;
	}
	
	public AbstractTreeItem getParent() {
		return parent;
	}
	
	public int getInsertPosition() {
		return insertPosition;
	}
	
	@Override
	public void revert() {
		//TODO
	}
}
