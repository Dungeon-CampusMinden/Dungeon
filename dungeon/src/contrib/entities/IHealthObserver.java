package contrib.entities;

import contrib.components.HealthComponent;
import core.Entity;

/**
 * IHealthObserver is an interface that represents an observer in the Observer design pattern.
 *
 * <p>This interface is specifically designed to observe health events in the game. It defines a
 * single method, onHealthEvent, which is called to notify the observer of health-related changes in
 * the entity it is observing.
 */
public interface IHealthObserver {
  /**
   * Called to notify the observer of health-related changes in the entity it is observing.
   *
   * @param entity The entity that the health event is related to.
   * @param healthComponent The health component of the entity.
   * @param healthEvent The type of health event (DAMAGE or DEATH).
   */
  void onHeathEvent(Entity entity, HealthComponent healthComponent, HealthEvent healthEvent);

  /**
   * HealthEvent is an enumeration that represents the type of health event.
   *
   * <p>It has the following values: - DAMAGE: Represents a damage event. - DEATH: Represents a
   * death event.
   */
  enum HealthEvent {
    DAMAGE,
    DEATH
  }
}
