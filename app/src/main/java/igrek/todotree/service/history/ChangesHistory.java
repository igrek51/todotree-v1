package igrek.todotree.service.history;


import java.util.LinkedList;
import java.util.List;

import igrek.todotree.service.history.change.AbstractItemChange;

//TODO: revert changes - save modifications, creating, removing, moving items
public class ChangesHistory {
	
	private LinkedList<AbstractItemChange> changes = new LinkedList<>();
	
	private boolean anyChange = false;
	
	public ChangesHistory() {
	}
	
	public List<AbstractItemChange> getChanges() {
		return changes;
	}
	
	public void addChange(AbstractItemChange change) {
		changes.add(change);
	}
	
	public void clear() {
		changes.clear();
		anyChange = false;
	}
	
	public void revertLast() {
		if (!changes.isEmpty()) {
			changes.pollLast().revert();
		}
	}
	
	public void registerChange() {
		anyChange = true;
	}
	
	public boolean hasChanges() {
		//TODO return !changes.isEmpty();
		return anyChange;
	}
}
