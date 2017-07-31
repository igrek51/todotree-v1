package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;

public class SavedNewItemEvent implements IEvent {
	
	private String content;
	
	public SavedNewItemEvent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
}
