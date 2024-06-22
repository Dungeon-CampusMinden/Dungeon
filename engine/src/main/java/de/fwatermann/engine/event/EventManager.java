package de.fwatermann.engine.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Manages the registration and firing of events.
 * This class is a singleton and provides methods to register and unregister event listeners, and to fire events.
 */
public class EventManager {

    private static EventManager instance;

    /**
     * Returns the singleton instance of the EventManager.
     *
     * @return the singleton instance of the EventManager
     */
    public static EventManager getInstance() {
        if(instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    private EventManager() {}

    private final Map<Class<? extends Event>, Set<EventHandlerPair>> listeners = new HashMap<>();

    /**
     * Registers an event listener.
     * This method inspects the methods of the listener for the EventHandler annotation and registers them for the appropriate event type.
     *
     * @param listener the event listener to register
     * @throws IllegalArgumentException if a method annotated with EventHandler has less or more than 1 parameter, or if the parameter is not a subclass of Event
     */
    public void registerListener(EventListener listener) {
        Method[] methods = listener.getClass().getMethods();
        Arrays.stream(methods).filter(m -> {
            return m.getAnnotation(EventHandler.class) != null;
        }).forEach(m -> {
            Class<?>[] params = m.getParameterTypes();
            if(params.length != 1) {
                throw new IllegalArgumentException("EventHandler-Method " + m.getName() + " in " + listener.getClass().getName() + " has less or more than 1 parameters!");
            }
            Class<?> eventTypeClass = params[0];
            if(!Event.class.isAssignableFrom(eventTypeClass)) {
                throw new IllegalArgumentException("EventHandler-Method " + m.getName() + " in " + listener.getClass().getName() + " has a parameter that is not a subclass of Event!");
            }
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) eventTypeClass;
            this.listeners.computeIfAbsent(eventType, k -> new HashSet<>()).add(new EventHandlerPair(listener, m));
        });
    }

    /**
     * Unregisters an event listener.
     * This method removes all event handler methods of the listener from the registration.
     *
     * @param listener the event listener to unregister
     */
    public void unregisterListener(EventListener listener) {
        this.listeners.values().forEach(l -> l.removeIf(p -> p.listener() == listener));
    }

    /**
     * Fires an event.
     * This method invokes all registered event handler methods for the type of the event.
     *
     * @param event the event to fire
     */
    public void fireEvent(Event event) {
        Set<EventHandlerPair> listeners = this.listeners.get(event.getClass());
        if(listeners == null) {
            return;
        }
        listeners.forEach(p -> {
            try {
                p.method().invoke(p.listener(), event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Represents a pair of an event listener and an event handler method.
     * This record is used to store the association between an event listener and one of its event handler methods.
     */
    public record EventHandlerPair(EventListener listener, Method method) { }

}