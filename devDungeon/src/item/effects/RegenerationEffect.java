package item.effects;

import contrib.components.HealthComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.MissingComponentException;

/**
 * The RegenerationEffect class is used to apply a regeneration effect to an entity. The
 * regeneration effect will heal the entity for a certain amount every second for a certain
 * duration.
 */
public class RegenerationEffect {
  private final int amountPerSecond;
  private final int duration;

  /**
   * Constructs a new RegenerationEffect.
   *
   * @param amountPerSecond The amount of health to heal per second.
   * @param duration The duration of the regeneration effect in seconds.
   */
  public RegenerationEffect(int amountPerSecond, int duration) {
    this.amountPerSecond = amountPerSecond;
    this.duration = duration;
  }

  /**
   * Applies the regeneration effect to the target entity.
   *
   * <p>The regeneration effect will heal the target entity for the specified amount every second
   * for the specified duration.
   *
   * @param target The entity to apply the regeneration effect to.
   */
  public void applyRegeneration(Entity target) {
    for (int i = 1; i < duration + 1; i++) { // apply Regeneration every second for durationSec
      EventScheduler.scheduleAction(
          () -> {
            HealthComponent healthComponent =
                target
                    .fetch(HealthComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(target, HealthComponent.class));
            if (healthComponent.isDead()) {
              return;
            }
            healthComponent.receiveHit(new Damage(-amountPerSecond, DamageType.HEAL, target));
          },
          1000L * i);
    }
  }
}
