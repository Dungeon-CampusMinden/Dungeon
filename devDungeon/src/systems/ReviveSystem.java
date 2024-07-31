package systems;

import components.ReviveComponent;
import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.SpikyComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Map;

/**
 * The ReviveSystem is responsible for reviving entities that have died. It checks if an entity is
 * dead and has a {@link ReviveComponent} attached. If the entity has a revive count greater than 0,
 * the entity will be revived after a certain delay.
 *
 * <p>While the entity is dead, the {@link AIComponent} and {@link SpikyComponent} are inactive.
 * After the entity is revived, the revive count is decremented by 1. If the entity has a revive
 * count of 0, it will not be revived anymore.
 *
 * @see ReviveComponent
 * @see DevHealthSystem
 */
public class ReviveSystem extends System {

  private static final long REVIVE_DELAY = 5000; //  5 seconds
  private final Map<Entity, Long> deadEntities = new HashMap<>();

  /** Constructs a new ReviveSystem. */
  public ReviveSystem() {
    super(ReviveComponent.class, HealthComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(ReviveComponent.class, HealthComponent.class)
        .forEach(
            entity -> {
              ReviveComponent reviveComponent =
                  entity
                      .fetch(ReviveComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, ReviveComponent.class));
              HealthComponent healthComponent =
                  entity
                      .fetch(HealthComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, HealthComponent.class));

              if (healthComponent.isDead() && reviveComponent.reviveCount() > 0) {
                if (deadEntities.containsKey(entity)) {
                  if (java.lang.System.currentTimeMillis() - deadEntities.get(entity)
                      >= REVIVE_DELAY) {
                    reviveEntity(entity, healthComponent, reviveComponent);
                  }
                } else {
                  // Entity just died, add to deadEntities and wait for REVIVE_DELAY
                  entity
                      .fetch(AIComponent.class)
                      .ifPresent((ai) -> ai.active(false)); // while dead, AI is inactive
                  entity
                      .fetch(SpikyComponent.class)
                      .ifPresent((spiky) -> spiky.active(false)); // while dead, spiky is inactive
                  deadEntities.put(entity, java.lang.System.currentTimeMillis());
                }
              }
            });
  }

  private void reviveEntity(
      Entity entity, HealthComponent healthComponent, ReviveComponent reviveComponent) {
    entity.fetch(AIComponent.class).ifPresent((ai) -> ai.active(true));
    entity.fetch(SpikyComponent.class).ifPresent((spiky) -> spiky.active(true));
    healthComponent.currentHealthpoints(healthComponent.maximalHealthpoints());
    reviveComponent.reviveCount(reviveComponent.reviveCount() - 1);
    healthComponent.clearDamage(); // prevent stacking damage while dead
    deadEntities.remove(entity);
  }
}
