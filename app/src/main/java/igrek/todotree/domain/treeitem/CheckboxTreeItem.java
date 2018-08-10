package igrek.todotree.domain.treeitem;


public class CheckboxTreeItem extends AbstractTreeItem {
	
	private String name;
	private boolean checked;
	
	public CheckboxTreeItem(AbstractTreeItem parent, String name, boolean checked) {
		super(parent);
		this.name = name;
		this.checked = checked;
	}
	
	@Override
	public CheckboxTreeItem clone() {
		return new CheckboxTreeItem(null, name, checked).copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String getTypeName() {
		return "checkbox";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
