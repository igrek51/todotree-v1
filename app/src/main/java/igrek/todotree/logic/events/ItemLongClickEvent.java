package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;

public class ItemLongClickEvent implements IEvent {
	
	private int position;
	
	public ItemLongClickEvent(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
}
