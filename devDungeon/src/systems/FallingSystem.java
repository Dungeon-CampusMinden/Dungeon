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
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.NoSuchElementException;
import utils.EntityUtils;

/**
 * The FallingSystem is responsible for handling entities that fall into {@link PitTile}s. Falling
 * into a pit tile results in the entity dying. The system checks if an entity is falling and
 * handles the falling event accordingly.
 *
 * <p>Entities that fall into a pit tile will receive a {@link Damage} with the amount of health
 * points they have left. If the entity is a player and the {@link #DEBUG_DONT_KILL} flag is set to
 * true, the player will be teleported to a safe tile instead of dying.
 *
 * @see PitTile
 * @see Damage
 * @see DevHealthSystem
 */
public class FallingSystem extends System {

  /** Flag to prevent the player from dying when falling into a pit tile. */
  public static boolean DEBUG_DONT_KILL = false;

  /** Constructs a new FallingSystem. */
  public FallingSystem() {
    super(PositionComponent.class, HealthComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().filter(this::filterFalling).forEach(this::handleFalling);
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
                teleportPlayerIfPossible();
                return;
              }
              hc.receiveHit(new Damage(hc.currentHealthpoints(), DamageType.FALL, entity));
            });
  }

  private void teleportPlayerIfPossible() {
    Point heroCoords = EntityUtils.getHeroPosition();
    if (heroCoords != null) {
      Tile tile = getSafeTile(heroCoords);
      Debugger.TELEPORT(tile);
    }
  }

  private Tile getSafeTile(Point heroCoords) {
    return Game.accessibleTilesInRange(heroCoords, 5).stream()
        .findFirst()
        .orElse(
            Game.randomTile(LevelElement.FLOOR)
                .orElseThrow(() -> new NoSuchElementException("No Floor Tile in the level.")));
  }
}
