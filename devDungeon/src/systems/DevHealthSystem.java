package systems;

import components.MagicShieldComponent;
import components.ReviveComponent;
import contrib.entities.IHealthObserver;
import contrib.systems.HealthSystem;
import contrib.utils.components.health.DamageType;
import java.util.stream.Stream;

public class DevHealthSystem extends HealthSystem {
  public DevHealthSystem() {
    super();
  }

  @Override
  public void execute() {
    this.entityStream()
        // Consider only entities that have a HealthComponent
        // Form triples (e, hc, dc)
        .map(this::buildDataObject)
        // Apply damage
        .map(this::applyDamage)
        // Filter all dead entities
        .filter(hsd -> hsd.hc().isDead())
        // Filter out revivable entities
        .filter(this::shouldDie)
        // Set DeathAnimation if possible and not yet set
        .map(this::activateDeathAnimation)
        // Filter by state of animation
        .filter(this::testDeathAnimationStatus)
        // Remove all dead entities
        .forEach(this::removeDeadEntities);
  }

  @Override
  protected HSData applyDamage(final HSData hsd) {
    MagicShieldComponent msc = hsd.e().fetch(MagicShieldComponent.class).orElse(null);
    if (msc == null || msc.isDepleted()) {
      return super.applyDamage(hsd);
    }
    msc.hit(Stream.of(DamageType.values()).mapToInt(hsd.hc()::calculateDamageOf).sum());
    hsd.hc().clearDamage();
    this.observers.forEach(
        observer -> observer.onHeathEvent(hsd.e(), hsd.hc(), IHealthObserver.HealthEvent.DAMAGE));
    return hsd;
  }

  private boolean shouldDie(final HSData hsd) {
    ReviveComponent reviveComponent = hsd.e().fetch(ReviveComponent.class).orElse(null);
    return reviveComponent == null || reviveComponent.reviveCount() <= 0;
  }
}
