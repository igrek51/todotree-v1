package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.datatree.TreeItem;

public class SelectedItemClickedEvent implements IEvent {

    private int position;
    private TreeItem item;
    private boolean checked;

    public SelectedItemClickedEvent(int position, TreeItem item, boolean checked) {
        this.position = position;
        this.item = item;
        this.checked = checked;
    }

    public int getPosition() {
        return position;
    }

    public TreeItem getItem() {
        return item;
    }

    public boolean isChecked() {
        return checked;
    }
}
