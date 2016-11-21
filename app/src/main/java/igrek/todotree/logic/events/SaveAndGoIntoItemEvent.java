package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.datatree.TreeItem;

public class SaveAndGoIntoItemEvent implements IEvent {

    private TreeItem editedItem;
    private String content;

    public SaveAndGoIntoItemEvent(TreeItem editedItem, String content) {
        this.editedItem = editedItem;
        this.content = content;
    }

    public TreeItem getEditedItem() {
        return editedItem;
    }

    public String getContent() {
        return content;
    }
}
