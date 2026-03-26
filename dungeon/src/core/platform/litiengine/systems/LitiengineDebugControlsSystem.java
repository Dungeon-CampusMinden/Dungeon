package core.platform.litiengine.systems;

import contrib.configuration.KeyboardConfig;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.platform.Platform;
import core.utils.Direction;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;

/**
 * Lightweight debug controls for the LITIENGINE host.
 *
 * <p>This intentionally contains only backend-neutral gameplay debug actions that are useful
 * during LITIENGINE testing. GDX/HUD-specific debugger features stay in the GDX debugger.
 */
public final class LitiengineDebugControlsSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineDebugControlsSystem.class);

  public LitiengineDebugControlsSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_OUT.value())) {
      zoomCamera(-0.2f);
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.value())) {
      zoomCamera(0.2f);
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.value())) {
      teleportToCursor();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.value())) {
      teleportToEndNeighbor();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.value())) {
      teleportToStart();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.value())) {
      loadNextLevel();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_OPEN_DOORS.value())) {
      openDoors();
    }

    // Intentionally no DEBUG_SPAWN_MONSTER here for now.
    // That path still depends on asset/draw initialization we do not want in the
    // lightweight LITIENGINE debug path yet.
  }

  private static void zoomCamera(float amount) {
    LOGGER.debug("Change Camera Zoom {}", amount);

    if (!Platform.camera().supportsZoom()) {
      LOGGER.debug("Camera zoom is not supported by the active backend.");
      return;
    }

    float newZoom = Math.max(0.1f, Platform.camera().zoom() + amount);
    Platform.camera().zoom(newZoom);

    LOGGER.debug("New Camera Zoom {}", Platform.camera().zoom());
  }

  private static void teleportToCursor() {
    LOGGER.info("TELEPORT TO CURSOR");
    teleport(SkillTools.cursorPositionAsPoint());
  }

  private static void teleportToStart() {
    LOGGER.info("TELEPORT TO START");
    Game.startTile().ifPresent(LitiengineDebugControlsSystem::teleport);
  }

  private static void loadNextLevel() {
    LOGGER.info("TELEPORT ON END");
    Game.endTiles().stream().findFirst().ifPresent(LitiengineDebugControlsSystem::teleport);
  }

  private static void teleportToEndNeighbor() {
    LOGGER.info("TELEPORT TO END");

    Game.endTiles()
      .stream()
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

          for (Coordinate coordinate : neighborTiles) {
            Tile tile = Game.tileAt(coordinate.toPoint()).orElse(null);
            if (tile != null && tile.isAccessible()) {
              teleport(tile);
              return;
            }
          }

          LOGGER.info("No accessible tile next to end tile found");
        });
  }

  private static void teleport(Tile targetLocation) {
    teleport(targetLocation.coordinate().toPoint());
  }

  private static void teleport(Point targetLocation) {
    Game.player()
      .ifPresent(
        player -> {
          PositionComponent pc =
            player
              .fetch(PositionComponent.class)
              .orElseThrow(
                () ->
                  MissingComponentException.build(
                    player, PositionComponent.class));

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

  private static void openDoors() {
    Game.endTiles().forEach(ExitTile::open);
    Game.allTiles(LevelElement.DOOR).forEach(door -> ((DoorTile) door).open());
  }
}
