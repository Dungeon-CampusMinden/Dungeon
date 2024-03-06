package systems;

import components.ReviveComponent;

public class HealthSystem extends contrib.systems.HealthSystem {
  public HealthSystem() {
    super();
  }

  @Override
  public void execute() {
    entityStream()
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

  private boolean shouldDie(final HSData hsd) {
    ReviveComponent reviveComponent = hsd.e().fetch(ReviveComponent.class).orElse(null);
    return reviveComponent == null || reviveComponent.reviveCount() <= 0;
  }
}
