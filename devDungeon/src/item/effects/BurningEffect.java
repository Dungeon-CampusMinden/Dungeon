package item.effects;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.MissingComponentException;
import systems.EventScheduler;

/**
 * The BurningEffect class represents a burning effect in the game. This effect applies damage to a
 * target entity over a specified duration.
 */
public class BurningEffect {
  private static final EventScheduler EVENT_SCHEDULER = EventScheduler.getInstance();
  private final float amountPerSecond;
  private final int duration;

  /**
   * Constructs a new BurningEffect with the specified amount of damage to apply per second and
   * duration.
   *
   * @param amountPerSecond The amount of damage to apply per second.
   * @param duration The duration of the burning effect in seconds.
   */
  public BurningEffect(float amountPerSecond, int duration) {
    this.amountPerSecond = amountPerSecond;
    this.duration = duration;
  }

  /**
   * Applies the burning effect to the specified target entity. The target entity's health is
   * decreased by the specified amount per second for the specified duration. The damage is applied
   * in intervals, where the interval is calculated based on the amount of damage per second. For
   * example, if the amount of damage per second is 0.5, the damage is applied every 2 seconds. Or,
   * if the amount of damage per second is 2, the damage is applied every 0.5 seconds.
   *
   * @param target The target entity to apply the burning effect to.
   */
  public void applyBurning(Entity target) {
    int damageInterval = (int) (1 / amountPerSecond);
    int totalDamageEvents = (int) (duration * amountPerSecond);

    for (int i = 1; i <= totalDamageEvents; i++) {
      EVENT_SCHEDULER.scheduleAction(
          () -> {
            HealthComponent healthComponent =
                target
                    .fetch(HealthComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(target, HealthComponent.class));
            if (healthComponent.isDead()) {
              return;
            }
            healthComponent.receiveHit(new Damage(1, DamageType.FIRE, null));
          },
          1000L * i * damageInterval);
    }
  }
}
