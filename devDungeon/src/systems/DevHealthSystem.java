package systems;

import components.MagicShieldComponent;
import components.ReviveComponent;
import contrib.components.HealthComponent;
import contrib.systems.HealthSystem;
import core.components.DrawComponent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DevHealthSystem is a subclass of HealthSystem that adds additional functionality to the
 * HealthSystem. It implements logic for a revive component that allows entities to be revived a
 * certain number of times before they die and are removed from the game. It also implements a magic
 * shield component that allows entities to absorb a certain amount of damage before taking damage
 * to their health component.
 *
 * @see HealthSystem
 * @see MagicShieldComponent
 * @see ReviveComponent
 */
public class DevHealthSystem extends HealthSystem {

  /**
   * Create a new DevHealthSystem, a subclass of HealthSystem with additional functionality.
   *
   * @see DevHealthSystem
   */
  public DevHealthSystem() {
    super();
  }

  @Override
  public void execute() {
    // filter entities for components and partition into alive and dead
    Map<Boolean, List<HSData>> deadOrAlive =
        filteredEntityStream(HealthComponent.class, DrawComponent.class)
            .map(
                e ->
                    new HSData(
                        e,
                        e.fetch(HealthComponent.class).orElseThrow(),
                        e.fetch(DrawComponent.class).orElseThrow()))
            .collect(Collectors.partitioningBy(hsd -> hsd.hc().isDead()));

    // apply damage to all entities which are still alive
    deadOrAlive.get(false).forEach(this::applyDamage);

    // handle dead entities
    deadOrAlive.get(true).stream()
        .map(this::activateDeathAnimation)
        .filter(this::isDeathAnimationFinished)
        .filter(this::shouldDie) // ignore entities that should not die
        .forEach(this::triggerOnDeath);
  }

  @Override
  protected HSData applyDamage(final HSData hsd) {
    hsd.e()
        .fetch(MagicShieldComponent.class)
        .ifPresent(
            msc -> {
              msc.hit(calculateDamage(hsd));
              hsd.hc().clearDamage();
            });
    return super.applyDamage(hsd);
  }

  private boolean shouldDie(final HSData hsd) {
    ReviveComponent reviveComponent = hsd.e().fetch(ReviveComponent.class).orElse(null);
    return reviveComponent == null || reviveComponent.reviveCount() <= 0;
  }
}
