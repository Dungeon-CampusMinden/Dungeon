package core.platform.litiengine.systems;

import contrib.configuration.KeyboardConfig;
import core.System;
import core.debug.DebugGameplayActions;
import core.input.Keys;
import core.platform.Platform;
import core.platform.litiengine.render.depth.LitiengineDepthLayerEffectPipeline;
import core.platform.litiengine.render.level.LitiengineLevelEffectPipeline;
import core.platform.litiengine.render.scene.LitienginePassthroughDebugEffect;
import core.platform.litiengine.render.scene.LitiengineSceneColorGradeEffect;
import core.platform.litiengine.render.scene.LitiengineSceneEffectPipeline;
import core.utils.InputManager;
import core.utils.Rectangle;
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

  private static final int TOGGLE_SCENE_EFFECTS_KEY = Keys.F7;
  private static final int TOGGLE_LEVEL_EFFECTS_KEY = Keys.F8;
  private static final int TOGGLE_DEPTH_LAYER_EFFECTS_KEY = Keys.F9;

  /**
   * Temporary backend-local key for toggling the passthrough alpha/transparency debug view.
   *
   * <p>This corresponds to the old {@code debugPMA} shader flag, but is re-modeled for the
   * LITIENGINE scene-pass image pipeline.
   */
  private static final int TOGGLE_PASSTHROUGH_PMA_KEY = Keys.F10;

  /**
   * Temporary backend-local key for toggling the passthrough world-position debug view.
   *
   * <p>This corresponds to the old {@code debugWorldPos} shader flag, but is re-modeled for the
   * LITIENGINE scene-pass image pipeline.
   */
  private static final int TOGGLE_PASSTHROUGH_WORLD_POS_KEY = Keys.F11;

  /**
   * Temporary backend-local key for starter-scoped regional scene color-grade verification.
   *
   * <p>Press F12 to enable/disable the dedicated starter demo effect.
   * Press Shift+F12 to switch that same demo between regional mode and global mode.
   */
  private static final int TOGGLE_REGIONAL_SCENE_COLOR_GRADE_KEY = Keys.F12;

  private static final String PASSTHROUGH_DEBUG_EFFECT_ID =
    "litiengine_debug_passthrough_scene_effect";

  private static final String STARTER_SCENE_COLOR_GRADE_DEMO_ID =
    "starter_scene_color_grade_demo";

  private static final Rectangle DEFAULT_STARTER_SCENE_COLOR_GRADE_REGION =
    new Rectangle(1f, 5f, 10f, 4f);

  private static final float DEFAULT_STARTER_SCENE_COLOR_GRADE_TRANSITION_SIZE = 2.0f;

  private Rectangle rememberedRegionalSceneColorGradeRegion = null;
  private float rememberedRegionalSceneColorGradeTransitionSize =
    DEFAULT_STARTER_SCENE_COLOR_GRADE_TRANSITION_SIZE;

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

    if (InputManager.isKeyJustPressed(TOGGLE_PASSTHROUGH_PMA_KEY)) {
      togglePassthroughPmaDebug();
    }

    if (InputManager.isKeyJustPressed(TOGGLE_PASSTHROUGH_WORLD_POS_KEY)) {
      togglePassthroughWorldPosDebug();
    }

    if (InputManager.isKeyJustPressed(TOGGLE_REGIONAL_SCENE_COLOR_GRADE_KEY)) {
      if (isShiftPressed()) {
        toggleRegionalSceneColorGradeRegionMode();
      } else {
        toggleRegionalSceneColorGradeEnabled();
      }
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

  private void togglePassthroughPmaDebug() {
    LitienginePassthroughDebugEffect effect = ensurePassthroughDebugEffectRegistered();
    boolean newState = !effect.debugPMA();
    effect.debugPMA(newState);
    syncPassthroughEnabledState(effect);

    LOGGER.info(
      "LITIENGINE passthrough alpha debug is now {}.",
      newState ? "enabled" : "disabled");
  }

  private void togglePassthroughWorldPosDebug() {
    LitienginePassthroughDebugEffect effect = ensurePassthroughDebugEffectRegistered();
    boolean newState = !effect.debugWorldPos();
    effect.debugWorldPos(newState);
    syncPassthroughEnabledState(effect);

    LOGGER.info(
      "LITIENGINE passthrough world-position debug is now {}.",
      newState ? "enabled" : "disabled");
  }

  private void toggleRegionalSceneColorGradeEnabled() {
    LitiengineSceneColorGradeEffect effect = starterSceneColorGradeDemoEffect();
    if (effect == null) {
      LOGGER.warn(
        "No starter regional scene color grade demo is registered under id '{}'.",
        STARTER_SCENE_COLOR_GRADE_DEMO_ID);
      return;
    }

    boolean newState = !effect.enabled();
    effect.enabled(newState);

    LOGGER.info(
      "LITIENGINE starter regional scene color grade is now {}.",
      newState ? "enabled" : "disabled");
  }

  private void toggleRegionalSceneColorGradeRegionMode() {
    LitiengineSceneColorGradeEffect effect = starterSceneColorGradeDemoEffect();
    if (effect == null) {
      LOGGER.warn(
        "No starter regional scene color grade demo is registered under id '{}'.",
        STARTER_SCENE_COLOR_GRADE_DEMO_ID);
      return;
    }

    Rectangle currentRegion = effect.region();
    if (currentRegion == null) {
      Rectangle restoreRegion =
        rememberedRegionalSceneColorGradeRegion != null
          ? copy(rememberedRegionalSceneColorGradeRegion)
          : copy(DEFAULT_STARTER_SCENE_COLOR_GRADE_REGION);

      float restoreTransition =
        rememberedRegionalSceneColorGradeTransitionSize > 0f
          ? rememberedRegionalSceneColorGradeTransitionSize
          : DEFAULT_STARTER_SCENE_COLOR_GRADE_TRANSITION_SIZE;

      effect.region(restoreRegion).transitionSize(restoreTransition);

      LOGGER.info(
        "LITIENGINE starter scene color grade verification is now in regional mode.");
      return;
    }

    rememberedRegionalSceneColorGradeRegion = copy(currentRegion);
    rememberedRegionalSceneColorGradeTransitionSize = effect.transitionSize();

    effect.region(null);

    LOGGER.info(
      "LITIENGINE starter scene color grade verification is now in global mode.");
  }

  private LitiengineSceneColorGradeEffect starterSceneColorGradeDemoEffect() {
    return LitiengineSceneEffectPipeline.effects()
      .get(STARTER_SCENE_COLOR_GRADE_DEMO_ID)
      .filter(LitiengineSceneColorGradeEffect.class::isInstance)
      .map(LitiengineSceneColorGradeEffect.class::cast)
      .orElse(null);
  }

  private static boolean isShiftPressed() {
    return InputManager.isKeyPressed(Keys.SHIFT_LEFT)
      || InputManager.isKeyPressed(Keys.SHIFT_RIGHT);
  }

  private LitienginePassthroughDebugEffect ensurePassthroughDebugEffectRegistered() {
    return LitiengineSceneEffectPipeline.effects()
      .get(PASSTHROUGH_DEBUG_EFFECT_ID)
      .filter(LitienginePassthroughDebugEffect.class::isInstance)
      .map(LitienginePassthroughDebugEffect.class::cast)
      .orElseGet(
        () -> {
          LitienginePassthroughDebugEffect effect = new LitienginePassthroughDebugEffect();
          effect.enabled(false);

          // High priority so the debug image is evaluated late in the scene-pass chain.
          LitiengineSceneEffectPipeline.effects().add(PASSTHROUGH_DEBUG_EFFECT_ID, effect, 10_000);
          return effect;
        });
  }

  private static void syncPassthroughEnabledState(LitienginePassthroughDebugEffect effect) {
    effect.enabled(effect.debugPMA() || effect.debugWorldPos());
  }

  private static Rectangle copy(Rectangle rectangle) {
    return new Rectangle(rectangle.x(), rectangle.y(), rectangle.width(), rectangle.height());
  }

  @Override
  public void stop() {
    // Cant be stopped
  }
}
