package igrek.todotree.model.treeitem;


public class SeparatorTreeItem extends AbstractTreeItem {
	
	public SeparatorTreeItem(AbstractTreeItem parent) {
		super(parent);
	}
	
	@Override
	public SeparatorTreeItem clone() {
		return new SeparatorTreeItem(null).copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		return "----------";
	}
	
	@Override
	public String getTypeName() {
		return "separator";
	}
}
