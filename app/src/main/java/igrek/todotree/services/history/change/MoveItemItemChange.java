package igrek.todotree.services.history.change;


import igrek.todotree.model.tree.TreeItem;

public class MoveItemItemChange extends AbstractItemChange {
	
	private TreeItem movedItem;
	private TreeItem parent;
	private int startPosition;
	private int endPosition;
	
	public MoveItemItemChange(TreeItem movedItem, TreeItem parent, int startPosition, int endPosition) {
		this.movedItem = movedItem;
		this.parent = parent;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public TreeItem getMovedItem() {
		return movedItem;
	}
	
	public TreeItem getParent() {
		return parent;
	}
	
	public int getStartPosition() {
		return startPosition;
	}
	
	public int getEndPosition() {
		return endPosition;
	}
	
	@Override
	public void revert() {
		//TODO
	}
}
