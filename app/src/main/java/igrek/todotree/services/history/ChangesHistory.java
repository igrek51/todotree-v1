package igrek.todotree.services.history;


import java.util.LinkedList;
import java.util.List;

import igrek.todotree.services.history.change.AbstractItemChange;

public class ChangesHistory {
	
	private LinkedList<AbstractItemChange> changes = new LinkedList<>();
	
	public ChangesHistory() {
	}
	
	public List<AbstractItemChange> getChanges() {
		return changes;
	}
	
	public void addChange(AbstractItemChange change) {
		changes.add(change);
	}
	
	public boolean hasChanges() {
		return !changes.isEmpty();
	}
	
	public void clear() {
		changes.clear();
	}
	
	public void revertLast() {
		if (!changes.isEmpty()) {
			changes.pollLast().revert();
		}
	}
}
