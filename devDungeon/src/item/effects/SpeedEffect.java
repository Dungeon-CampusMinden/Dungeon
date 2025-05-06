package item.effects;

import contrib.systems.EventScheduler;
import core.Entity;

/**
 * Provides a mechanism to apply a temporary speed increase effect to an entity within the game.
 * Using the {@link EventScheduler}, this effect increases the entity's speed for a designated
 * duration before reverting it back to its original state. The implementation relies on scheduling
 * both the application of the speed increase and its later reversal.
 */
public interface SpeedEffect {

  /**
   * Applies a temporary speed increase to the target entity, then reverts its speed to normal after
   * the specified duration. The increase in speed is applied immediately, and its reversal will be
   * scheduled to occur after the duration expires.
   *
   * @param target The entity to which the speed effect will be applied.
   * @see EventScheduler
   */
  void applySpeedEffect(Entity target);
}
