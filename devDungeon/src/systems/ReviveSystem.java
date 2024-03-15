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

public class ReviveSystem extends System {

  private static final long REVIVE_DELAY = 5000; //  5 seconds
  private final Map<Entity, Long> deadEntities = new HashMap<>();

  public ReviveSystem() {
    super(ReviveComponent.class);
  }

  @Override
  public void execute() {
    this.entityStream()
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
                if (this.deadEntities.containsKey(entity)) {
                  if (java.lang.System.currentTimeMillis() - this.deadEntities.get(entity)
                      >= REVIVE_DELAY) {
                    entity.fetch(AIComponent.class).ifPresent((ai) -> ai.active(true));
                    entity.fetch(SpikyComponent.class).ifPresent((spiky) -> spiky.active(true));
                    healthComponent.currentHealthpoints(healthComponent.maximalHealthpoints());
                    reviveComponent.reviveCount(reviveComponent.reviveCount() - 1);
                    this.deadEntities.remove(entity);
                  }
                } else {
                  // Entity just died, add to deadEntities and wait for REVIVE_DELAY
                  entity
                      .fetch(AIComponent.class)
                      .ifPresent((ai) -> ai.active(false)); // while dead, AI is inactive
                  entity
                      .fetch(SpikyComponent.class)
                      .ifPresent((spiky) -> spiky.active(false)); // while dead, spiky is inactive
                  this.deadEntities.put(entity, java.lang.System.currentTimeMillis());
                }
              }
            });
  }
}
