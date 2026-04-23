package contrib.debug.systems;

import core.game.render.effects.BaseColorGradeEffect;
import core.utils.Rectangle;
import core.utils.logging.DungeonLogger;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Controller for debug color-grade effect toggles.
 *
 * <p>The class enables toggling of effect states, switching between regional and
 * global modes, and managing effect configurations such as regions and transition sizes.
 *
 * <p>Instances of this class are immutable except for internally maintained state
 * related to remembered configurations.
 */
final class DebugColorGradeController {

  private final String effectId;
  private final String passLabel;
  private final Rectangle defaultRegion;
  private final float defaultTransitionSize;
  private final Supplier<? extends BaseColorGradeEffect<?>> effectSupplier;

  private Rectangle rememberedRegion = null;
  private float rememberedTransitionSize;

  DebugColorGradeController(
    String effectId,
    String passLabel,
    Rectangle defaultRegion,
    float defaultTransitionSize,
    Supplier<? extends BaseColorGradeEffect<?>> effectSupplier) {
    this.effectId = Objects.requireNonNull(effectId, "effectId");
    this.passLabel = Objects.requireNonNull(passLabel, "passLabel");
    this.defaultRegion = Rectangle.copyOf(Objects.requireNonNull(defaultRegion, "defaultRegion"));
    this.defaultTransitionSize = defaultTransitionSize;
    this.effectSupplier = Objects.requireNonNull(effectSupplier, "effectSupplier");
    this.rememberedTransitionSize = defaultTransitionSize;
  }

  void toggleEnabled(DungeonLogger logger) {
    BaseColorGradeEffect<?> effect = effectOrWarn(logger);
    if (effect == null) {
      return;
    }

    boolean newState = !effect.enabled();
    effect.enabled(newState);

    logger.info(
      "Starter regional {} color grade is now {}.",
      passLabel,
      newState ? "enabled" : "disabled");
  }

  void toggleRegionMode(DungeonLogger logger) {
    BaseColorGradeEffect<?> effect = effectOrWarn(logger);
    if (effect == null) {
      return;
    }

    Rectangle currentRegion = effect.region();
    if (currentRegion == null) {
      effect.region(restoreRegion()).transitionSize(restoreTransitionSize());

      logger.info(
        "Starter {} color grade verification is now in regional mode.",
        passLabel);
      return;
    }

    rememberedRegion = Rectangle.copyOf(currentRegion);
    rememberedTransitionSize = effect.transitionSize();

    effect.region(null);

    logger.info(
      "Starter {} color grade verification is now in global mode.",
      passLabel);
  }

  private BaseColorGradeEffect<?> effectOrWarn(DungeonLogger logger) {
    BaseColorGradeEffect<?> effect = effectSupplier.get();
    if (effect != null) {
      return effect;
    }

    logger.warn(
      "No starter regional {} color grade demo is registered under id '{}'.",
      passLabel,
      effectId);
    return null;
  }

  private Rectangle restoreRegion() {
    return rememberedRegion != null
      ? Rectangle.copyOf(rememberedRegion)
      : Rectangle.copyOf(defaultRegion);
  }

  private float restoreTransitionSize() {
    return rememberedTransitionSize > 0f
      ? rememberedTransitionSize
      : defaultTransitionSize;
  }
}
