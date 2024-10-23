package de.fwatermann.dungine.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the registration and firing of events. This class is a singleton and provides methods to
 * register and unregister event listeners, and to fire events.
 */
public class EventManager {

  private static final Logger LOGGER = LogManager.getLogger(EventManager.class);

  private static EventManager instance;

  /**
   * Returns the singleton instance of the EventManager.
   *
   * @return the singleton instance of the EventManager
   */
  public static EventManager getInstance() {
    if (instance == null) {
      instance = new EventManager();
    }
    return instance;
  }

  private EventManager() {}

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<Class<? extends Event>, Set<EventHandlerPair>> listeners = new HashMap<>();

  /**
   * Registers an event listener. This method inspects the methods of the listener for the
   * EventHandler annotation and registers them for the appropriate event type.
   *
   * @param listener the event listener to register
   * @throws IllegalArgumentException if a method annotated with EventHandler has less or more than
   *     1 parameter, or if the parameter is not a subclass of Event
   */
  public void registerListener(de.fwatermann.dungine.event.EventListener listener) {
    List<Method> methods = new ArrayList<>();
    Class<?> clazz = listener.getClass();
    while (clazz != null) {
      methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
      clazz = clazz.getSuperclass();
    }
    methods.stream()
        .filter(m -> m.getAnnotation(EventHandler.class) != null)
        .forEach(
            m -> {
              if (Modifier.isPrivate(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) {
                m.setAccessible(true);
              }

              Class<?>[] params = m.getParameterTypes();
              if (params.length != 1) {
                throw new IllegalArgumentException(
                    "EventHandler-Method "
                        + m.getName()
                        + " in "
                        + listener.getClass().getName()
                        + " has less or more than 1 parameters!");
              }
              Class<?> eventTypeClass = params[0];
              if (!Event.class.isAssignableFrom(eventTypeClass)) {
                throw new IllegalArgumentException(
                    "EventHandler-Method "
                        + m.getName()
                        + " in "
                        + listener.getClass().getName()
                        + " has a parameter that is not a subclass of Event!");
              }
              @SuppressWarnings("unchecked")
              Class<? extends Event> eventType = (Class<? extends Event>) eventTypeClass;
              try {
                this.lock.writeLock().lock();
                this.listeners
                    .computeIfAbsent(eventType, k -> new HashSet<>())
                    .add(new EventHandlerPair(listener, listener.getClass(), m));
              } finally {
                this.lock.writeLock().unlock();
              }
              LOGGER.debug(
                  "Registered event handler for event type {} by {}",
                  eventType.getName(),
                  listener.getClass().getName());
            });
  }

  /**
   * Registers a static event listener. This method inspects the methods of the listener class for
   * the EventHandler annotation and registers them for the appropriate event type.
   *
   * <p>This method is useful for registering event listeners that are not instantiated, meaning the
   * event handler methods are static.
   *
   * @param pClazz the class of the event listener to register
   */
  public void registerStaticListener(Class<? extends EventListener> pClazz) {
    List<Method> methods = new ArrayList<>();
    Class<?> clazz = pClazz;
    while (clazz != null) {
      methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
      clazz = clazz.getSuperclass();
    }
    methods.stream()
        .filter(
            m -> m.getAnnotation(EventHandler.class) != null && Modifier.isStatic(m.getModifiers()))
        .forEach(
            m -> {
              if (Modifier.isPrivate(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) {
                m.setAccessible(true);
              }

              Class<?>[] params = m.getParameterTypes();
              if (params.length != 1) {
                throw new IllegalArgumentException(
                    "EventHandler-Method "
                        + m.getName()
                        + " in "
                        + pClazz.getName()
                        + " has less or more than 1 parameters!");
              }
              Class<?> eventTypeClass = params[0];
              if (!Event.class.isAssignableFrom(eventTypeClass)) {
                throw new IllegalArgumentException(
                    "EventHandler-Method "
                        + m.getName()
                        + " in "
                        + pClazz.getName()
                        + " has a parameter that is not a subclass of Event!");
              }
              @SuppressWarnings("unchecked")
              Class<? extends Event> eventType = (Class<? extends Event>) eventTypeClass;
              try {
                this.lock.writeLock().lock();
                this.listeners
                    .computeIfAbsent(eventType, k -> new HashSet<>())
                    .add(new EventHandlerPair(null, pClazz, m));
              } finally {
                this.lock.writeLock().unlock();
              }
              LOGGER.debug(
                  "Registered static event handler for event type {} by {}",
                  eventType.getName(),
                  pClazz.getName());
            });
  }

  /**
   * Unregisters an event listener. This method removes all event handler methods of the listener
   * from the registration.
   *
   * @param listener the event listener to unregister
   */
  public void unregisterListener(de.fwatermann.dungine.event.EventListener listener) {
    try {
      this.lock.writeLock().lock();
      this.listeners.forEach(
          (k, v) ->
              v.removeIf(
                  p -> {
                    if (p.listener == listener) {
                      LOGGER.debug(
                          "Unregistered {} handler for object of {} [{}]",
                          k.getName(),
                          listener.getClass().getName(),
                          listener.hashCode());
                      return true;
                    }
                    return false;
                  }));
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /**
   * Unregisters a static event listener. This method removes all event handler methods of the
   * specified listener class from the registration.
   *
   * @param listenerClass the class of the event listener to unregister
   */
  public void unregisterStaticListener(Class<? extends EventListener> listenerClass) {
    try {
      this.lock.writeLock().lock();
      this.listeners
          .values()
          .forEach(
              l ->
                  l.removeIf(
                      p -> {
                        if (p.clazz() == listenerClass) {
                          LOGGER.debug(
                              "Unregistered all static handlers for class {}",
                              listenerClass.getName());
                          return true;
                        }
                        return false;
                      }));
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  /**
   * Fires an event. This method invokes all registered event handler methods for the type of the
   * event.
   *
   * @param event the event to fire
   */
  public void fireEvent(Event event) {
    try {
      this.lock.readLock().lock();
      Set<EventHandlerPair> listeners = this.listeners.get(event.getClass());
      if (listeners == null) {
        return;
      }
      listeners.forEach(
          p -> {
            try {
              p.method().invoke(p.listener(), event);
            } catch (InvocationTargetException | IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          });
    } finally {
      this.lock.readLock().unlock();
    }
  }

  /**
   * Represents a pair of an event listener and an event handler method. This record is used to
   * store the association between an event listener and one of its event handler methods.
   *
   * @param listener The EventListener object, that contains the event handler method
   * @param clazz The class of the EventListener
   * @param method The event handler method
   */
  public record EventHandlerPair(
      EventListener listener, Class<? extends EventListener> clazz, Method method) {}
}
