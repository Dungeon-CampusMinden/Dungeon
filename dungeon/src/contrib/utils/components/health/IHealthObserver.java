package contrib.utils.components.health;

import contrib.systems.HealthSystem;

/**
 * This interface is specifically designed to observe health events in the game. It defines a single
 * method, onHealthEvent, which is called to notify the observer of health-related changes in the
 * entity it is observing.
 *
 * <p>It is used by the {@link contrib.systems.HealthSystem HealthSystem} to notify observers of
 * health events.
 *
 * @see HealthEvent
 */
public interface IHealthObserver {
  /**
   * Called to notify the observer of health-related changes in the entity it is observing.
   *
   * @param hsData The data of the entity with HealthComponent and DrawComponent.
   * @param healthEvent The type of health event (DAMAGE or DEATH).
   */
  void onHealthEvent(HealthSystem.HSData hsData, HealthEvent healthEvent);

  /**
   * HealthEvent is an enumeration that represents the type of health event.
   *
   * <p>It has the following values:
   *
   * <ul>
   *   <li>DAMAGE: Represents a damage event.
   *   <li>DEATH: Represents a death event.
   * </ul>
   */
  enum HealthEvent {
    /** Represents a damage event. */
    DAMAGE,
    /** Represents a death event. */
    DEATH
  }
}
