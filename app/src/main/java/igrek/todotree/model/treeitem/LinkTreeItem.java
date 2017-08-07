package igrek.todotree.model.treeitem;


import com.google.common.base.Joiner;

import java.util.ArrayList;
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
	
	public LinkTreeItem(AbstractTreeItem parent, AbstractTreeItem target, String name) {
		super(parent);
		this.name = name;
		setTarget(target);
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
				return "> " + targetPath.replace("\t", "/");
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
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTargetPath() {
		return targetPath;
	}
	
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	
	public AbstractTreeItem getTarget() {
		String[] paths = targetPath.split("\\t");
		return new TreeController().findItemByPath(paths);
	}
	
	public void setTarget(AbstractTreeItem target) {
		Joiner joiner = Joiner.on("\t");
		List<String> names = new ArrayList<>();
		AbstractTreeItem current = target;
		do {
			//except root item
			if (current instanceof RootTreeItem)
				break;
			names.add(current.getDisplayName());
			current = current.getParent();
		} while (current != null);
		targetPath = joiner.join(names);
	}
	
	public boolean isBroken() {
		return getTarget() == null;
	}
	
}
