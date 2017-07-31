package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.datatree.TreeItem;

public class CancelEditedItemEvent implements IEvent {
	
	private TreeItem editedItem;
	
	public CancelEditedItemEvent(TreeItem editedItem) {
		this.editedItem = editedItem;
	}
	
	public TreeItem getEditedItem() {
		return editedItem;
	}
}
