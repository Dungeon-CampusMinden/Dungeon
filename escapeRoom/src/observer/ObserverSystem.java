package observer;

import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * ObserverSystem implements a simple observer pattern.
 *
 * <p>It maintains a mapping between {@link ObservableComponent} instances and the set of {@link
 * ObserverComponent} instances that should be notified when the observable changes.
 *
 * <p>During each execution of the system, it iterates over all entities with an {@link
 * ObservableComponent}, checks if they should notify their observers, and then triggers the {@link
 * ObserverComponent#onNotify()} callbacks for all registered observers.
 *
 * <p>Use {@link #observe} to ann an observer and use {@link #unobserve} to remove an observer.
 */
public class ObserverSystem extends System {

  private static HashMap<ObservableComponent, Set<ObserverComponent>> observation = new HashMap<>();

  /**
   * Creates a new ObserverSystem that will operate on entities containing {@link
   * ObservableComponent}.
   */
  public ObserverSystem() {
    super(ObservableComponent.class);
  }

  /**
   * Executes the observer notifications.
   *
   * <p>For each entity with an {@link ObservableComponent} that returns true for {@link
   * ObservableComponent#shouldNotify()}, all registered observers are notified by calling their
   * {@link ObserverComponent#onNotify()} method.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .map(this::buildDataObject)
        .filter(this::shouldNotify)
        .forEach(this::notifyObservers);
  }

  /**
   * Registers an observer to be notified when the given observable triggers.
   *
   * @param observer the observer to register
   * @param observed the observable to watch
   */
  public static void observe(ObserverComponent observer, ObservableComponent observed) {
    observation.computeIfAbsent(observed, key -> new HashSet<>()).add(observer);
  }

  /**
   * Unregisters an observer from the given observable.
   *
   * <p>If the observer was the last one for the observable, the observable entry is removed from
   * the internal map.
   *
   * @param observer the observer to remove
   * @param observed the observable to stop watching
   */
  public static void unobserve(ObserverComponent observer, ObservableComponent observed) {
    Set<ObserverComponent> observers = observation.get(observed);
    if (observers != null) {
      observers.remove(observer);
      if (observers.isEmpty()) {
        observation.remove(observed);
      }
    }
  }

  /** Clears all registered observations. */
  public static void clear() {
    observation.clear();
  }

  /**
   * Checks if the observable component indicates it should notify its observers.
   *
   * @param osData the data object containing the observable component
   * @return true if the observable should notify, false otherwise
   */
  private boolean shouldNotify(OSData osData) {
    return osData.oc().shouldNotify().get();
  }

  /**
   * Notifies all observers registered for the given observable component.
   *
   * @param osData the data object containing the entity and its observable component
   */
  private void notifyObservers(OSData osData) {
    Set<ObserverComponent> observers = observation.get(osData.oc());
    if (observers != null) {
      observers.forEach(observer -> observer.onNotify().accept(osData.e()));
    }
  }

  /**
   * Builds an internal data object for easier processing in the system.
   *
   * @param e the entity to build data for
   * @return an {@link OSData} record containing the entity and its observable component
   * @throws MissingComponentException if the entity does not contain an ObservableComponent
   */
  private OSData buildDataObject(Entity e) {
    ObservableComponent oc =
        e.fetch(ObservableComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, ObservableComponent.class));

    return new OSData(e, oc);
  }

  private record OSData(Entity e, ObservableComponent oc) {}
}
