package hotload;

import core.Entity;
import core.components.VelocityComponent;
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

  @Override
  public void applySpeedEffect(Entity target) {
    VelocityComponent vc = target.fetch(VelocityComponent.class).get();
    vc.xVelocity(vc.xVelocity() + speedIncrease);
    vc.yVelocity(vc.yVelocity() + speedIncrease);
  }
}
