package igrek.todotree.domain.treeitem;


public class TextTreeItem extends AbstractTreeItem {
	
	private String name;
	
	public TextTreeItem(AbstractTreeItem parent, String name) {
		super(parent);
		this.name = name;
	}
	
	public TextTreeItem(String name) {
		this(null, name);
	}
	
	@Override
	public TextTreeItem clone() {
		return new TextTreeItem(null, name).copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String getTypeName() {
		return "text";
	}
	
	public void setName(String name) {
		this.name = name;
	}
}