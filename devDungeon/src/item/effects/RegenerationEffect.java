package item.effects;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.MissingComponentException;
import systems.EventScheduler;

public class RegenerationEffect {
  private static final EventScheduler EVENT_SCHEDULER = EventScheduler.getInstance();
  private final int amountPerSecond;
  private final int duration;

  public RegenerationEffect(int amountPerSecond, int duration) {
    this.amountPerSecond = amountPerSecond;
    this.duration = duration;
  }

  public void applyRegeneration(Entity target) {
    for (int i = 1; i < this.duration + 1; i++) { // apply Regeneration every second for durationSec
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
            healthComponent.receiveHit(new Damage(-this.amountPerSecond, DamageType.HEAL, null));
          },
          1000L * i);
    }
  }
}
