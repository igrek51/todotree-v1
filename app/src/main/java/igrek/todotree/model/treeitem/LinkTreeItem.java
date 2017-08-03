package igrek.todotree.model.treeitem;


public class LinkTreeItem extends AbstractTreeItem {
	
	private AbstractTreeItem target;
	private String name;
	
	public LinkTreeItem(AbstractTreeItem parent, AbstractTreeItem target, String name) {
		super(parent);
		this.target = target;
		this.name = name;
	}
	
	@Override
	public LinkTreeItem clone() {
		return new LinkTreeItem(null, target, name).copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		if (name == null)
			return "> " + target.getDisplayName();
		return "> " + name;
	}
	
	@Override
	public String getTypeName() {
		return "link";
	}
	
	public AbstractTreeItem getTarget() {
		return target;
	}
	
	public String getTargetPath() {
		//TODO
		return "/" + getDisplayName();
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
