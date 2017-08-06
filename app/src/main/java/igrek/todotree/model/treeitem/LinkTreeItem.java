package igrek.todotree.model.treeitem;


import igrek.todotree.controller.TreeController;

public class LinkTreeItem extends AbstractTreeItem {
	
	private String targetPath;
	private String name;
	
	public LinkTreeItem(AbstractTreeItem parent, String targetPath, String name) {
		super(parent);
		this.targetPath = targetPath;
		this.name = name;
	}
	
	@Override
	public LinkTreeItem clone() {
		return new LinkTreeItem(null, targetPath, name).copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		if (name == null) {
			AbstractTreeItem target = getTarget();
			if (target == null) {
				return "> " + targetPath;
			} else {
				return "> " + target.getDisplayName();
			}
		}
		return "> " + name;
	}
	
	@Override
	public String getTypeName() {
		return "link";
	}
	
	public AbstractTreeItem getTarget() {
		//TODO forbid using character "/" in item names or something
		String[] paths = targetPath.split("//");
		return new TreeController().findItemByPath(paths);
	}
	
	public boolean isBroken() {
		return getTarget() == null;
	}
	
	public String getDisplayTargetPath() {
		return getTargetPath().replace("//", "/");
	}
	
	public String getTargetPath() {
		return targetPath;
	}
	
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
