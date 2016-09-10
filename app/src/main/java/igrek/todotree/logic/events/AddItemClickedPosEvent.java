package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;

public class AddItemClickedPosEvent implements IEvent {

    private int position;

    public AddItemClickedPosEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
