package igrek.todotree.logic.events;

import igrek.todotree.logic.controller.dispatcher.IEvent;

public class ItemMovedEvent implements IEvent {

    private int position;

    private int step;

    public ItemMovedEvent(int position, int step) {
        this.position = position;
        this.step = step;
    }

    public int getPosition() {
        return position;
    }

    public int getStep() {
        return step;
    }
}
