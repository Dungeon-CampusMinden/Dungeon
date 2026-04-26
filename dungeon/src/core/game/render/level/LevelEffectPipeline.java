package core.game.render.level;

import java.awt.image.BufferedImage;

/**
 * A pipeline for applying effects to the rendered level layer.
 *
 * <p>This class manages a centralized registry of level effects and provides methods to apply all
 * registered effects to a rendered level image. Effects are applied sequentially in their
 * configured order.
 */
public final class LevelEffectPipeline {

  private static final LevelEffectRegistry EFFECTS = new LevelEffectRegistry();

  private LevelEffectPipeline() {}

  /**
   * Gets the global level effect registry.
   *
   * @return the effect registry
   */
  public static LevelEffectRegistry effects() {
    return EFFECTS;
  }

  /**
   * Checks whether any level effects are currently enabled.
   *
   * @return true if at least one effect is enabled, false otherwise
   */
  public static boolean hasEnabledEffects() {
    return EFFECTS.hasEnabledEffects();
  }

  /**
   * Checks whether all level effects are enabled.
   *
   * @return true if all registered effects are enabled, false otherwise
   */
  public static boolean allEnabled() {
    return EFFECTS.allEnabled();
  }

  /**
   * Toggles the enabled state of all level effects.
   *
   * @return the new enabled state after toggling
   */
  public static boolean toggleAll() {
    return EFFECTS.toggleAll();
  }

  /**
   * Applies all enabled level effects to the provided image in order.
   *
   * @param source the source image to apply effects to (can be null)
   * @param context the level pass context containing rendering information
   * @param nowMs the current timestamp in milliseconds
   * @return the processed image with all effects applied, or the original image if no effects are
   *     enabled
   * @throws IllegalStateException if an effect returns null
   */
  public static BufferedImage apply(BufferedImage source, LevelPassContext context, long nowMs) {
    if (source == null || !hasEnabledEffects()) {
      return source;
    }

    BufferedImage current = source;
    for (LevelEffect effect : EFFECTS.getEnabledSorted()) {
      current = effect.apply(current, context, nowMs);
      if (current == null) {
        throw new IllegalStateException(
            effect.getClass().getSimpleName() + " returned null level image.");
      }
    }

    return current;
  }

  /** Clears all registered level effects. */
  public static void clear() {
    EFFECTS.clear();
  }
}
