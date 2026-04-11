package core.platform.litiengine.systems;

import contrib.configuration.KeyboardConfig;
import core.System;
import core.debug.DebugGameplayActions;
import core.input.Keys;
import core.platform.Platform;
import core.platform.litiengine.render.depth.LitiengineDepthLayerEffectPipeline;
import core.platform.litiengine.render.level.LitiengineLevelEffectPipeline;
import core.platform.litiengine.render.scene.LitiengineSceneEffectPipeline;
import core.utils.InputManager;
import core.utils.logging.DungeonLogger;

/**
 * Lightweight debug controls for the LITIENGINE host.
 *
 * <p>This intentionally contains only backend-neutral gameplay debug actions that are useful
 * during LITIENGINE testing. GDX/HUD-specific debugger features stay in the GDX debugger.
 *
 * <p>Scene-pass, level-pass and depth-layer-pass verification are intentionally backend-local here,
 * because the corresponding pass pipelines also currently exist only on the LITIENGINE render path.
 */
public final class LitiengineDebugControlsSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineDebugControlsSystem.class);

  /**
   * Temporary backend-local key for toggling all registered scene-pass effects.
   *
   * <p>This is intentionally not routed through {@link KeyboardConfig} yet, because this commit is
   * only about lightweight debug verification of the new LITIENGINE-only scene effect pipeline.
   */
  private static final int TOGGLE_SCENE_EFFECTS_KEY = Keys.F7;

  /**
   * Temporary backend-local key for toggling all registered level-pass effects.
   *
   * <p>This is intentionally not routed through {@link KeyboardConfig} yet, because this commit is
   * only about lightweight debug verification of the new LITIENGINE-only level effect pipeline.
   */
  private static final int TOGGLE_LEVEL_EFFECTS_KEY = Keys.F8;

  /**
   * Temporary backend-local key for toggling all registered depth-layer-pass effects.
   *
   * <p>This is intentionally not routed through {@link KeyboardConfig} yet, because this commit is
   * only about lightweight debug verification of the new LITIENGINE-only depth-layer effect
   * pipeline.
   */
  private static final int TOGGLE_DEPTH_LAYER_EFFECTS_KEY = Keys.F9;

  public LitiengineDebugControlsSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_HUD.value())) {
      Platform.render().toggleDebugHud();
    }

    if (InputManager.isKeyJustPressed(TOGGLE_SCENE_EFFECTS_KEY)) {
      boolean enabled = LitiengineSceneEffectPipeline.toggleAll();
      LOGGER.info(
        "LITIENGINE scene-pass effects are now {}.",
        enabled ? "enabled" : "disabled");
    }

    if (InputManager.isKeyJustPressed(TOGGLE_LEVEL_EFFECTS_KEY)) {
      boolean enabled = LitiengineLevelEffectPipeline.toggleAll();
      LOGGER.info(
        "LITIENGINE level-pass effects are now {}.",
        enabled ? "enabled" : "disabled");
    }

    if (InputManager.isKeyJustPressed(TOGGLE_DEPTH_LAYER_EFFECTS_KEY)) {
      boolean enabled = LitiengineDepthLayerEffectPipeline.toggleAll();
      LOGGER.info(
        "LITIENGINE depth-layer-pass effects are now {}.",
        enabled ? "enabled" : "disabled");
    }

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
  }

  @Override
  public void stop() {
    // Cant be stopped
  }
}
