package contrib.debug;

import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.platform.Platform;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;

/**
 * Backend-neutral gameplay debug actions shared by different host-specific debugger systems.
 *
 * <p>This class intentionally contains only actions that do not depend on backend-specific HUD or
 * rendering classes. Host-specific debugger systems can delegate to these helpers and keep only
 * truly backend-specific functionality locally.
 */
public final class DebugGameplayActions {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DebugGameplayActions.class);

  private DebugGameplayActions() {}

  /**
   * Zooms the active camera in or out by the given amount if the current backend supports camera
   * zooming.
   *
   * @param amount zoom delta
   */
  public static void zoomCamera(float amount) {
    LOGGER.debug("Change Camera Zoom {}", amount);

    if (!Platform.camera().supportsZoom()) {
      LOGGER.debug("Camera zoom is not supported by the active backend.");
      return;
    }

    float newZoom = Math.max(0.1f, Platform.camera().zoom() + amount);
    Platform.camera().zoom(newZoom);

    LOGGER.debug("New Camera Zoom {}", Platform.camera().zoom());
  }

  /** Teleports the player to the current cursor position. */
  public static void teleportToCursor() {
    LOGGER.info("TELEPORT TO CURSOR");
    teleport(SkillTools.cursorPositionAsPoint());
  }

  /** Teleports the player to the level start tile. */
  public static void teleportToStart() {
    LOGGER.info("TELEPORT TO START");
    Game.startTile().ifPresent(DebugGameplayActions::teleport);
  }

  /** Teleports the player onto an end tile so the next level can be triggered. */
  public static void teleportToExit() {
    LOGGER.info("TELEPORT ON END");
    Game.endTiles().stream().findFirst().ifPresent(DebugGameplayActions::teleport);
  }

  /** Teleports the player next to an end tile if an accessible neighboring tile exists. */
  public static void teleportToEndNeighbor() {
    LOGGER.info("TELEPORT TO END");

    Game.endTiles().stream()
        .findFirst()
        .ifPresent(
            end -> {
              Coordinate endTile = end.coordinate();
              Coordinate[] neighborTiles = {
                endTile.translate(Direction.UP),
                endTile.translate(Direction.DOWN),
                endTile.translate(Direction.LEFT),
                endTile.translate(Direction.RIGHT),
              };

              for (Coordinate neighborTile : neighborTiles) {
                Tile targetTile = Game.tileAt(neighborTile).orElse(null);
                if (targetTile == null || !targetTile.isAccessible()) {
                  continue;
                }

                teleport(targetTile);
                break;
              }
            });
  }

  /**
   * Teleports the player to the given tile.
   *
   * @param targetTile tile to teleport to
   */
  public static void teleport(Tile targetTile) {
    teleport(targetTile.coordinate().toPoint());
  }

  /**
   * Teleports the player to the given location if the tile exists and is accessible.
   *
   * @param targetLocation target point
   */
  public static void teleport(Point targetLocation) {
    Game.player()
        .ifPresent(
            player -> {
              PositionComponent pc =
                  player
                      .fetch(PositionComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(player, PositionComponent.class));

              LOGGER.info("Attempting to teleport to {}", targetLocation);
              Tile t = Game.tileAt(targetLocation).orElse(null);
              if (t == null || !t.isAccessible()) {
                LOGGER.info("Cannot teleport to non-existing or non-accessible tile");
                return;
              }

              pc.position(targetLocation);
              LOGGER.info("Teleport successful");
            });
  }

  /** Opens all exit and door tiles at the current level. */
  public static void openDoors() {
    Game.endTiles().forEach(ExitTile::open);
    Game.allTiles(LevelElement.DOOR).forEach(door -> ((DoorTile) door).open());
  }
}
