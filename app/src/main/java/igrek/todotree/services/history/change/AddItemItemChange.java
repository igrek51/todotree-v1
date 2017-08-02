package igrek.todotree.services.history.change;


import igrek.todotree.datatree.item.TreeItem;

public class AddItemItemChange extends AbstractItemChange {
	
	private TreeItem newItem;
	private TreeItem parent;
	private int insertPosition;
	
	public AddItemItemChange(TreeItem newItem, TreeItem parent, int insertPosition) {
		this.newItem = newItem;
		this.parent = parent;
		this.insertPosition = insertPosition;
	}
	
	public TreeItem getNewItem() {
		return newItem;
	}
	
	public TreeItem getParent() {
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
