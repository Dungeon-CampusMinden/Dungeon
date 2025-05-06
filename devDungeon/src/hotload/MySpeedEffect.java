package hotload;

import contrib.systems.EventScheduler;
import core.Entity;
import item.effects.SpeedEffect;

public class MySpeedEffect extends SpeedEffect {

  /**
   * Initializes a new instance of the SpeedEffect with a specified increase in speed and duration.
   *
   * @param speedIncrease The amount to increase the entity's speed by.
   * @param duration The duration, in seconds, for which the speed increase is applied.
   */
  public MySpeedEffect(float speedIncrease, int duration) {
    super(speedIncrease, duration);
  }

  /**
   * Applies a temporary speed increase to the target entity, then reverts its speed to normal after
   * the specified duration. The increase in speed is applied immediately, and its reversal will be
   * scheduled to occur after the duration expires.
   *
   * <p>TODO: Implement the applySpeedEffect method to schedule the speed increase and its
   * reversion.
   *
   * @param target The entity to which the speed effect will be applied.
   * @see EventScheduler
   */
  @Override
  public void applySpeedEffect(Entity target) {
    throw new UnsupportedOperationException();
  }
}
