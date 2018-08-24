package igrek.todotree.service.history;

import java.util.HashMap;
import java.util.Map;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.LinkTreeItem;

public class LinkHistoryService {
	
	private Map<AbstractTreeItem, LinkTreeItem> target2link = new HashMap<>();
	
	public LinkHistoryService() {
	}
	
	public void storeTargetLink(AbstractTreeItem target, LinkTreeItem link) {
		target2link.put(target, link);
	}
	
	public LinkTreeItem getLinkFromTarget(AbstractTreeItem target) {
		return target2link.get(target);
	}
	
	public boolean hasLink(AbstractTreeItem target) {
		return target2link.containsKey(target);
	}
	
	public void resetTarget(AbstractTreeItem target) {
		target2link.remove(target);
	}
}
