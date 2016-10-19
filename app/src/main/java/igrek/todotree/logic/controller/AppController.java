package igrek.todotree.logic.controller;

import igrek.todotree.logic.controller.dispatcher.EventDispatcher;
import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.controller.dispatcher.IEventObserver;
import igrek.todotree.logic.controller.services.IService;
import igrek.todotree.logic.controller.services.ServicesRegistry;

public class AppController {

    private ServicesRegistry servicesRegistry;

    private EventDispatcher eventDispatcher;

    private static AppController instance = null;

    /**
     * Reset instacji rejestru usług i wyczyszczenie listenerów eventów
     */
    public AppController() {
        servicesRegistry = new ServicesRegistry();
        eventDispatcher = new EventDispatcher();
        instance = this;
    }

    private static AppController getInstance() {
        if (instance == null) {
            new AppController();
        }
        return instance;
    }


    public static <T extends IService> void registerService(T service) {
        getInstance().servicesRegistry.registerService(service);
    }

    public static <T extends IService> T getService(Class<T> clazz) {
        return getInstance().servicesRegistry.getService(clazz);
    }

    public static void registerEventObserver(Class<? extends IEvent> eventClass, IEventObserver observer) {
        getInstance().eventDispatcher.registerEventObserver(eventClass, observer);
    }

    //TODO czyszczenie nie dla wszystkich tylko dla konkretnych odbiorców
    public static void clearEventObservers(Class<? extends IEvent> eventClass) {
        getInstance().eventDispatcher.clearEventObservers(eventClass);
    }

    public static void sendEvent(IEvent event) {
        getInstance().eventDispatcher.sendEvent(event);
    }
}
