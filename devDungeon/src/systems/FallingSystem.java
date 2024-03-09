package systems;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;

public class FallingSystem extends System {

  public FallingSystem() {
    super(PositionComponent.class, HealthComponent.class);
  }

  @Override
  public void execute() {
    entityStream().filter(this::filterFalling).forEach(this::handleFalling);
  }

  private boolean filterFalling(Entity entity) {
    Point entityPosition =
        entity
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Tile currentTile = Game.tileAT(entityPosition);
    if (currentTile instanceof PitTile pitTile) {
      return pitTile.isOpen();
    }
    return false;
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
