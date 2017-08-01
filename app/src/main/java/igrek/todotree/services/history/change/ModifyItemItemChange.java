package igrek.todotree.services.history.change;


import igrek.todotree.services.datatree.TreeItem;

public class ModifyItemItemChange extends AbstractItemChange {
	
	private TreeItem modifiedItem;
	private String oldContent;
	private String newContent;
	
	public ModifyItemItemChange(TreeItem modifiedItem, String oldContent, String newContent) {
		this.modifiedItem = modifiedItem;
		this.oldContent = oldContent;
		this.newContent = newContent;
	}
	
	public TreeItem getModifiedItem() {
		return modifiedItem;
	}
	
	public String getOldContent() {
		return oldContent;
	}
	
	public String getNewContent() {
		return newContent;
	}
	
	@Override
	public void revert() {
		//TODO
	}
}
