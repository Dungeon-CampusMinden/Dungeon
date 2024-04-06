package systems;

import contrib.components.HealthComponent;
import contrib.utils.components.Debugger;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.NoSuchElementException;
import utils.EntityUtils;

public class FallingSystem extends System {

  public static boolean DEBUG_DONT_KILL = false;

  public FallingSystem() {
    super(PositionComponent.class, HealthComponent.class);
  }

  @Override
  public void execute() {
    this.entityStream().filter(this::filterFalling).forEach(this::handleFalling);
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
              if (DEBUG_DONT_KILL && entity.isPresent(PlayerComponent.class)) {
                this.teleportPlayerIfPossible();
                return;
              }
              hc.receiveHit(new Damage(hc.currentHealthpoints(), DamageType.FALL, entity));
            });
  }

  private void teleportPlayerIfPossible() {
    Point heroCoords = EntityUtils.getHeroPosition();
    if (heroCoords != null) {
      Tile tile = this.getSafeTile(heroCoords);
      Debugger.TELEPORT(tile);
    }
  }

  private Tile getSafeTile(Point heroCoords) {
    Tile tile;
    try {
      tile = LevelUtils.accessibleTilesInRange(heroCoords, 5).getFirst();
    } catch (NoSuchElementException e) {
      tile = Game.randomTile(LevelElement.FLOOR);
    }
    return tile;
  }
}
