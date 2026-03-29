package core.platform.litiengine.systems;

import contrib.configuration.KeyboardConfig;
import core.System;
import core.debug.DebugGameplayActions;
import core.utils.InputManager;
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
      DebugGameplayActions.zoomCamera(-0.2f);
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.value())) {
      DebugGameplayActions.zoomCamera(0.2f);
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.value())) {
      DebugGameplayActions.teleportToCursor();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.value())) {
      DebugGameplayActions.teleportToEndNeighbor();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.value())) {
      DebugGameplayActions.teleportToStart();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.value())) {
      DebugGameplayActions.loadNextLevel();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_OPEN_DOORS.value())) {
      DebugGameplayActions.openDoors();
    }

    // Intentionally no DEBUG_SPAWN_MONSTER here for now.
    // That path still depends on asset/draw initialization we do not want in the
    // lightweight LITIENGINE debug path yet.
  }
 }
