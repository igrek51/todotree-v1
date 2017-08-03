package igrek.todotree.services.history.change;


import igrek.todotree.model.treeitem.AbstractTreeItem;

public class MoveItemItemChange extends AbstractItemChange {
	
	private AbstractTreeItem movedItem;
	private AbstractTreeItem parent;
	private int startPosition;
	private int endPosition;
	
	public MoveItemItemChange(AbstractTreeItem movedItem, AbstractTreeItem parent, int startPosition, int endPosition) {
		this.movedItem = movedItem;
		this.parent = parent;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public AbstractTreeItem getMovedItem() {
		return movedItem;
	}
	
	public AbstractTreeItem getParent() {
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
