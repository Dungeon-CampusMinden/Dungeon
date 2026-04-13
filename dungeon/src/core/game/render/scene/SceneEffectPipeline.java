package core.game.render.scene;

import java.awt.image.BufferedImage;

/**
 * Global scene-pass effect pipeline for the LITIENGINE backend.
 *
 * <p>Effects in this pipeline are applied to the fully rendered frame, after ECS world rendering
 * and UI overlay rendering have produced a complete scene image.
 */
public final class SceneEffectPipeline {

  private static final SceneEffectRegistry EFFECTS = new SceneEffectRegistry();

  private SceneEffectPipeline() {}

  /** @return the global scene effect registry */
  public static SceneEffectRegistry effects() {
    return EFFECTS;
  }

  /** @return true if at least one scene effect is enabled */
  public static boolean hasEnabledEffects() {
    return EFFECTS.hasEnabledEffects();
  }

  /** @return true if all toggleable scene effects are currently enabled */
  public static boolean allEnabled() {
    return EFFECTS.allEnabled();
  }

  /**
   * Toggles all toggleable scene effects at once.
   *
   * @return the new enabled state that was applied
   */
  public static boolean toggleAll() {
    return EFFECTS.toggleAll();
  }

  /**
   * Applies all enabled scene effects in priority order.
   *
   * @param source fully rendered frame image
   * @param nowMs current timestamp in milliseconds
   * @return processed frame image
   */
  public static BufferedImage apply(BufferedImage source, long nowMs) {
    if (source == null || !hasEnabledEffects()) {
      return source;
    }

    BufferedImage current = source;
    for (SceneEffect effect : EFFECTS.getEnabledSorted()) {
      current = effect.apply(current, nowMs);
      if (current == null) {
        throw new IllegalStateException(
          effect.getClass().getSimpleName() + " returned null scene image.");
      }
    }

    return current;
  }

  /** Clears all registered scene effects. */
  public static void clear() {
    EFFECTS.clear();
  }
}
