package igrek.todotree.model.treeitem;


import com.google.common.base.Joiner;

import java.util.List;

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
				return getDisplayTargetPath();
			} else {
				return target.getDisplayName();
			}
		}
		return name;
	}
	
	@Override
	public String getTypeName() {
		return "link";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTargetPath() {
		return targetPath;
	}
	
	public String getDisplayTargetPath() {
		return "/" + targetPath.replace("\t", "/");
	}
	
	public AbstractTreeItem getTarget() {
		String[] paths = targetPath.split("\\t");
		return new TreeController().findItemByPath(paths);
	}
	
	public void setTarget(AbstractTreeItem target) {
		Joiner joiner = Joiner.on("\t");
		List<String> names = target.getNamesPaths();
		targetPath = joiner.join(names);
	}
	
	public void setTarget(AbstractTreeItem targetParent, String targetName) {
		Joiner joiner = Joiner.on("\t");
		List<String> names = targetParent.getNamesPaths();
		names.add(targetName);
		targetPath = joiner.join(names);
	}
	
	public boolean isBroken() {
		return getTarget() == null;
	}
	
}
