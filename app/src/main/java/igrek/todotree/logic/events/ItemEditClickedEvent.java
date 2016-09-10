package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.datatree.TreeItem;

public class ItemEditClickedEvent implements IEvent {

    private int position;
    private TreeItem treeItem;

    public ItemEditClickedEvent(int position, TreeItem treeItem) {
        this.position = position;
        this.treeItem = treeItem;
    }

    public int getPosition() {
        return position;
    }

    public TreeItem getTreeItem() {
        return treeItem;
    }
}
