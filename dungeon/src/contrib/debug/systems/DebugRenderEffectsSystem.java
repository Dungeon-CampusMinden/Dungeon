package contrib.debug.systems;

import contrib.configuration.KeyboardConfig;
import contrib.debug.controls.DebugColorGradeController;
import core.System;
import core.input.Keys;
import core.game.render.depth.DepthLayerColorGradeEffect;
import core.game.render.depth.DepthLayerEffectPipeline;
import core.game.render.level.LevelColorGradeEffect;
import core.game.render.level.LevelEffectPipeline;
import contrib.debug.effects.DebugPassthroughEffect;
import core.game.render.scene.SceneColorGradeEffect;
import core.game.render.scene.SceneEffectPipeline;
import core.utils.InputManager;
import core.utils.Rectangle;
import core.utils.components.draw.DepthLayer;
import core.utils.logging.DungeonLogger;

/**
 * A debug system that handles keyboard input for toggling various rendering effects and debug
 * features.
 *
 * <p>This system allows developers to enable/disable effects for scenes, levels, and depth layers,
 * as well as various debug visualization modes. It manages regional color-grade effects with
 * configurable regions and transition sizes.
 *
 * <p>Key bindings:
 * <ul>
 *   <li>F5: Toggle regional depth-layer color grade
 *   <li>F6: Toggle regional level color grade
 *   <li>F7: Toggle regional scene color grade (Shift+F7 to switch modes)
 *   <li>F8: Toggle scene effects
 *   <li>F9: Toggle level effects
 *   <li>F10: Toggle depth-layer effects
 *   <li>F11: Toggle passthrough alpha/transparency debug view
 *   <li>F12: Toggle passthrough world-position debug view
 * </ul>
 */
public final class DebugRenderEffectsSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(DebugRenderEffectsSystem.class);

  private static final String PASSTHROUGH_DEBUG_EFFECT_ID =
    "litiengine_debug_passthrough_scene_effect";

  private static final String STARTER_SCENE_COLOR_GRADE_DEMO_ID =
    "starter_scene_color_grade_demo";

  private static final String STARTER_LEVEL_COLOR_GRADE_DEMO_ID =
    "starter_level_color_grade_demo";

  private static final String STARTER_DEPTH_COLOR_GRADE_DEMO_ID =
    "starter_depth_color_grade_demo";

  private static final Rectangle DEFAULT_STARTER_SCENE_COLOR_GRADE_REGION =
    new Rectangle(1f, 5f, 10f, 4f);

  private static final Rectangle DEFAULT_STARTER_LEVEL_COLOR_GRADE_REGION =
    new Rectangle(3f, 0f, 7f, 4f);

  private static final Rectangle DEFAULT_STARTER_DEPTH_COLOR_GRADE_REGION =
    new Rectangle(7f, 5f, 3f, 3f);

  private static final int STARTER_DEPTH_COLOR_GRADE_DEMO_LAYER =
    DepthLayer.ForegroundDeco.depth();

  private static final float DEFAULT_STARTER_SCENE_COLOR_GRADE_TRANSITION_SIZE = 2.0f;
  private static final float DEFAULT_STARTER_LEVEL_COLOR_GRADE_TRANSITION_SIZE = 2.0f;
  private static final float DEFAULT_STARTER_DEPTH_COLOR_GRADE_TRANSITION_SIZE = 1.5f;

  private final DebugColorGradeController regionalSceneColorGrade =
    new DebugColorGradeController(
      STARTER_SCENE_COLOR_GRADE_DEMO_ID,
      "scene",
      DEFAULT_STARTER_SCENE_COLOR_GRADE_REGION,
      DEFAULT_STARTER_SCENE_COLOR_GRADE_TRANSITION_SIZE,
      this::starterSceneColorGradeDemoEffect);

  private final DebugColorGradeController regionalLevelColorGrade =
    new DebugColorGradeController(
      STARTER_LEVEL_COLOR_GRADE_DEMO_ID,
      "level",
      DEFAULT_STARTER_LEVEL_COLOR_GRADE_REGION,
      DEFAULT_STARTER_LEVEL_COLOR_GRADE_TRANSITION_SIZE,
      this::starterLevelColorGradeDemoEffect);

  private final DebugColorGradeController regionalDepthColorGrade =
    new DebugColorGradeController(
      STARTER_DEPTH_COLOR_GRADE_DEMO_ID,
      "depth-layer",
      DEFAULT_STARTER_DEPTH_COLOR_GRADE_REGION,
      DEFAULT_STARTER_DEPTH_COLOR_GRADE_TRANSITION_SIZE,
      this::starterDepthColorGradeDemoEffect);

  /**
   * Constructs a new instance of the DebugRenderEffectsSystem.
   *
   * <p>This system is configured to operate on the client-side and is primarily used
   * for debugging and rendering various visual effects within the application.
   */
  public DebugRenderEffectsSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(
      KeyboardConfig.DEBUG_RENDER_REGIONAL_DEPTH_COLOR_GRADE.value())) {
      if (isShiftPressed()) {
        regionalDepthColorGrade.toggleRegionMode(LOGGER);
      } else {
        regionalDepthColorGrade.toggleEnabled(LOGGER);
      }
    }

    if (InputManager.isKeyJustPressed(
      KeyboardConfig.DEBUG_RENDER_REGIONAL_LEVEL_COLOR_GRADE.value())) {
      if (isShiftPressed()) {
        regionalLevelColorGrade.toggleRegionMode(LOGGER);
      } else {
        regionalLevelColorGrade.toggleEnabled(LOGGER);
      }
    }

    if (InputManager.isKeyJustPressed(
      KeyboardConfig.DEBUG_RENDER_REGIONAL_SCENE_COLOR_GRADE.value())) {
      if (isShiftPressed()) {
        regionalSceneColorGrade.toggleRegionMode(LOGGER);
      } else {
        regionalSceneColorGrade.toggleEnabled(LOGGER);
      }
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_RENDER_SCENE_EFFECTS.value())) {
      boolean enabled = SceneEffectPipeline.toggleAll();
      LOGGER.info("Scene-pass effects are now {}.", enabled ? "enabled" : "disabled");
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_RENDER_LEVEL_EFFECTS.value())) {
      boolean enabled = LevelEffectPipeline.toggleAll();
      LOGGER.info("Level-pass effects are now {}.", enabled ? "enabled" : "disabled");
    }

    if (InputManager.isKeyJustPressed(
      KeyboardConfig.DEBUG_RENDER_DEPTH_LAYER_EFFECTS.value())) {
      if (isShiftPressed()) {
        toggleDemoDepthLayerEffectGroup();
      } else {
        boolean enabled = DepthLayerEffectPipeline.toggleAll();
        LOGGER.info(
          "Depth-layer-pass effects are now {}.",
          enabled ? "enabled" : "disabled");
      }
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_RENDER_PASSTHROUGH_ALPHA.value())
      && !isShiftPressed()) {
      togglePassthroughPmaDebug();
    }

    if (InputManager.isKeyJustPressed(
      KeyboardConfig.DEBUG_RENDER_PASSTHROUGH_WORLD_POSITION.value())) {
      togglePassthroughWorldPosDebug();
    }
  }

  private void togglePassthroughPmaDebug() {
    DebugPassthroughEffect effect = ensurePassthroughDebugEffectRegistered();
    boolean newState = !effect.debugPMA();
    effect.debugPMA(newState);
    syncPassthroughEnabledState(effect);

    LOGGER.info(
      "Passthrough alpha debug is now {}.",
      newState ? "enabled" : "disabled");
  }

  private void togglePassthroughWorldPosDebug() {
    DebugPassthroughEffect effect = ensurePassthroughDebugEffectRegistered();
    boolean newState = !effect.debugWorldPos();
    effect.debugWorldPos(newState);
    syncPassthroughEnabledState(effect);

    LOGGER.info(
      "Passthrough world-position debug is now {}.",
      newState ? "enabled" : "disabled");
  }

  private void toggleDemoDepthLayerEffectGroup() {
    if (!DepthLayerEffectPipeline.hasEffects(STARTER_DEPTH_COLOR_GRADE_DEMO_LAYER)) {
      LOGGER.warn(
        "No depth-layer effects are registered for starter demo layer '{}'.",
        STARTER_DEPTH_COLOR_GRADE_DEMO_LAYER);
      return;
    }

    boolean enabled =
      DepthLayerEffectPipeline.toggleAll(STARTER_DEPTH_COLOR_GRADE_DEMO_LAYER);

    LOGGER.info(
      "Starter demo depth-layer effect group on layer '{}' is now {}.",
      STARTER_DEPTH_COLOR_GRADE_DEMO_LAYER,
      enabled ? "enabled" : "disabled");
  }

  private DepthLayerColorGradeEffect starterDepthColorGradeDemoEffect() {
    return DepthLayerEffectPipeline.effects(STARTER_DEPTH_COLOR_GRADE_DEMO_LAYER)
      .get(STARTER_DEPTH_COLOR_GRADE_DEMO_ID)
      .filter(DepthLayerColorGradeEffect.class::isInstance)
      .map(DepthLayerColorGradeEffect.class::cast)
      .orElse(null);
  }

  private SceneColorGradeEffect starterSceneColorGradeDemoEffect() {
    return SceneEffectPipeline.effects()
      .get(STARTER_SCENE_COLOR_GRADE_DEMO_ID)
      .filter(SceneColorGradeEffect.class::isInstance)
      .map(SceneColorGradeEffect.class::cast)
      .orElse(null);
  }

  private LevelColorGradeEffect starterLevelColorGradeDemoEffect() {
    return LevelEffectPipeline.effects()
      .get(STARTER_LEVEL_COLOR_GRADE_DEMO_ID)
      .filter(LevelColorGradeEffect.class::isInstance)
      .map(LevelColorGradeEffect.class::cast)
      .orElse(null);
  }

  private static boolean isShiftPressed() {
    return InputManager.isKeyPressed(Keys.SHIFT_LEFT)
      || InputManager.isKeyPressed(Keys.SHIFT_RIGHT);
  }

  private DebugPassthroughEffect ensurePassthroughDebugEffectRegistered() {
    return SceneEffectPipeline.effects()
      .get(PASSTHROUGH_DEBUG_EFFECT_ID)
      .filter(DebugPassthroughEffect.class::isInstance)
      .map(DebugPassthroughEffect.class::cast)
      .orElseGet(
        () -> {
          DebugPassthroughEffect effect = new DebugPassthroughEffect();
          effect.enabled(false);

          SceneEffectPipeline.effects().add(PASSTHROUGH_DEBUG_EFFECT_ID, effect, 10_000);
          return effect;
        });
  }

  private static void syncPassthroughEnabledState(DebugPassthroughEffect effect) {
    effect.enabled(effect.debugPMA() || effect.debugWorldPos());
  }

  @Override
  public void stop() {
    // Cannot be stopped
  }
}
