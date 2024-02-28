package contrib.systems;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;

public class FallingSystem extends System {
  @Override
  public void execute() {
    entityStream().filter(this::filterFalling).forEach(this::handleFalling);
  }

  private boolean filterFalling(Entity entity) {
    Point entityPosition =
        entity.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
    if (entityPosition == null) return false;
    Tile currentTile = Game.tileAT(entityPosition);
    return currentTile.levelElement().equals(LevelElement.SKIP);
  }

  private void handleFalling(Entity entity) {
    LOGGER.info("Entity " + entity + " has fallen to its death");
    entity
        .fetch(HealthComponent.class)
        .ifPresent(
            hc -> {
              hc.receiveHit(
                  new Damage(hc.currentHealthpoints(), DamageType.FALL, entity)); // kill the entity
            });
  }
}
