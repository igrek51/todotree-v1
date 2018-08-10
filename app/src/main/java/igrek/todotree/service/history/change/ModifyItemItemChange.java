package igrek.todotree.service.history.change;


import igrek.todotree.domain.treeitem.AbstractTreeItem;

public class ModifyItemItemChange extends AbstractItemChange {
	
	private AbstractTreeItem modifiedItem;
	private String oldContent;
	private String newContent;
	
	public ModifyItemItemChange(AbstractTreeItem modifiedItem, String oldContent, String newContent) {
		this.modifiedItem = modifiedItem;
		this.oldContent = oldContent;
		this.newContent = newContent;
	}
	
	public AbstractTreeItem getModifiedItem() {
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
