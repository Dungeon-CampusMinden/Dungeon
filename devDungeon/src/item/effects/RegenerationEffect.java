package item.effects;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.MissingComponentException;
import item.EffectScheduler;

public class RegenerationEffect {
  private static final EffectScheduler effectScheduler = EffectScheduler.getInstance();
  private final int amountPerSecond;
  private final int duration;

  public RegenerationEffect(int amountPerSecond, int duration) {
    this.amountPerSecond = amountPerSecond;
    this.duration = duration;
  }

  public void applyRegeneration(Entity target) {
    for (int i = 1; i < this.duration + 1; i++) { // apply Regeneration every second for durationSec
      effectScheduler.scheduleAction(
          () ->
              target
                  .fetch(HealthComponent.class)
                  .orElseThrow(() -> MissingComponentException.build(target, HealthComponent.class))
                  .receiveHit(new Damage(-this.amountPerSecond, DamageType.HEAL, null)),
          1000L * i);
    }
  }
}
