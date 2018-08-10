package igrek.todotree.domain.treeitem;


import com.google.common.base.Joiner;

import java.util.List;

import igrek.todotree.commands.TreeCommand;

public class LinkTreeItem extends AbstractTreeItem {
	
	private String targetPath;
	private String customName;
	
	public LinkTreeItem(AbstractTreeItem parent, String targetPath, String customName) {
		super(parent);
		this.targetPath = targetPath;
		this.customName = customName;
	}
	
	@Override
	public LinkTreeItem clone() {
		return new LinkTreeItem(null, targetPath, customName).copyChildren(this);
	}
	
	@Override
	public String getDisplayName() {
		if (hasCustomName())
			return customName;
		
		AbstractTreeItem target = getTarget();
		if (target == null) {
			return getDisplayTargetPath();
		} else {
			return target.getDisplayName();
		}
	}
	
	@Override
	public String getTypeName() {
		return "link";
	}
	
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	
	public String getCustomName() {
		return customName;
	}
	
	public boolean hasCustomName() {
		return customName != null;
	}
	
	public String getTargetPath() {
		return targetPath;
	}
	
	public String getDisplayTargetPath() {
		return "/" + targetPath.replace("\t", "/");
	}
	
	public AbstractTreeItem getTarget() {
		String[] paths = targetPath.split("\\t");
		return new TreeCommand().findItemByPath(paths);
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
