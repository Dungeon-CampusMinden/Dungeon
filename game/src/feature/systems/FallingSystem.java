package feature.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.Tile;
import engine.level.elements.tile.PitTile;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.components.MissingComponentException;
import engine.utils.logging.DungeonLogger;
import feature.components.CollideComponent;
import feature.components.Debugger;
import feature.components.FlyComponent;
import feature.components.HealthComponent;
import feature.health.Damage;
import feature.health.DamageType;
import feature.utils.EntityUtils;
import java.util.NoSuchElementException;
import java.util.Optional;

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
 */
public class FallingSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(FallingSystem.class);

  /** Flag to prevent the player from dying when falling into a pit tile. */
  public static boolean DEBUG_DONT_KILL = false;

  /** Constructs a new FallingSystem. */
  public FallingSystem() {
    super(PositionComponent.class, HealthComponent.class, VelocityComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().filter(this::filterFalling).forEach(this::handleFalling);
  }

  private boolean filterFalling(Entity entity) {
    if (entity.isPresent(FlyComponent.class)) return false;
    CollideComponent cc =
        entity
            .fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, CollideComponent.class));
    Point center = cc.collider().absoluteCenter();
    Tile tile = Game.tileAt(center).orElse(null);
    if (tile instanceof PitTile pitTile) {
      return pitTile.isOpen();
    }
    return false;
  }

  private void handleFalling(Entity entity) {
    LOGGER.info("Entity {} has fallen to its death", entity);
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
    Point playerCoords = EntityUtils.getPlayerPosition();
    if (playerCoords != null) {
      getSafeTile(playerCoords)
          .ifPresentOrElse(Debugger::TELEPORT, () -> LOGGER.warn("No safe place to teleport."));
    }
  }

  private Optional<Tile> getSafeTile(Point playerCoords) throws NoSuchElementException {
    try {
      return Optional.of(Game.accessibleTilesInRange(playerCoords, 5).getFirst());
    } catch (NoSuchElementException e) {
      return Game.randomTile(LevelElement.FLOOR);
    }
  }
}
