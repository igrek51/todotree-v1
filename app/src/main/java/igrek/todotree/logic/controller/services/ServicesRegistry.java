package igrek.todotree.logic.controller.services;

import java.util.HashMap;

import igrek.todotree.logger.Logs;

public class ServicesRegistry {

    private HashMap<String, IService> services;

    public ServicesRegistry() {
        services = new HashMap<>();
    }

    public <T extends IService> void registerService(T service) {
        String className = service.getClass().getName();
        services.put(className, service);
        Logs.debug("Service " + className + " registered");
    }

    public <T extends IService> T getService(Class<T> clazz) {
        try {
            T service = (T) services.get(clazz.getName());
            return service;
        } catch (ClassCastException e) {
            Logs.error("Error while casting service " + clazz.getName());
            return null;
        } catch (NullPointerException e) {
            Logs.error("Service " + clazz.getName() + " was not found");
            return null;
        }
    }
}
