package igrek.todotree.logic.controller;

import igrek.todotree.logic.controller.dispatcher.EventDispatcher;
import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.controller.dispatcher.IEventObserver;

public class AppController {

    private EventDispatcher eventDispatcher;

    private static AppController instance = null;

    /**
     * Reset instacji rejestru usług i wyczyszczenie listenerów eventów
     */
    public AppController() {
        eventDispatcher = new EventDispatcher();
        instance = this;
    }

    private static AppController getInstance() {
        if (instance == null) {
            new AppController();
        }
        return instance;
    }
	
	
	public static void registerEventObserver(Class<? extends IEvent> eventClass, IEventObserver observer) {
        getInstance().eventDispatcher.registerEventObserver(eventClass, observer);
    }

    //TODO poprawić strukturę aplikacji tak, aby nie musieć czyścić observerów
    //TODO czyszczenie nie dla wszystkich tylko dla konkretnych odbiorców
    @Deprecated
    public static void clearEventObservers(Class<? extends IEvent> eventClass) {
        getInstance().eventDispatcher.clearEventObservers(eventClass);
    }

    public static void sendEvent(IEvent event) {
        getInstance().eventDispatcher.sendEvent(event);
    }
}
