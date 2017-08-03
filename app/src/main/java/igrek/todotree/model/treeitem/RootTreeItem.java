package igrek.todotree.model.treeitem;


public class RootTreeItem extends AbstractTreeItem {
	
	public RootTreeItem() {
		super(null);
	}
	
	@Override
	public RootTreeItem clone() {
		return new RootTreeItem().copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		return "/";
	}
	
	@Override
	public String getTypeName() {
		return "/";
	}
	
	@Override
	public AbstractTreeItem getParent() {
		return null;
	}
}
