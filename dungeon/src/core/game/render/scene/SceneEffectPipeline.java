package core.game.render.scene;

import java.awt.image.BufferedImage;

/**
 * Utility class for managing and applying scene effects in a rendering pipeline.
 *
 * <p>This class provides a centralized mechanism to register, toggle, and apply post-processing
 * effects to rendered scene images. Scene effects are prioritized and applied sequentially in the
 * order defined by their priority and insertion sequence in the {@link SceneEffectRegistry}.
 *
 * <p>The {@code SceneEffectPipeline} is a global accessor to operations such as toggling all effects,
 * applying enabled effects, and managing the effect registry. It primarily interacts with
 * {@link SceneEffectRegistry} and ensures that all operations are consistent with the underlying
 * registry.
 *
 * <p>This class is not intended to be instantiated and contains only static utility methods.
 */
public final class SceneEffectPipeline {

  private static final SceneEffectRegistry EFFECTS = new SceneEffectRegistry();

  private SceneEffectPipeline() {}

  /**
   * Retrieves the global {@link SceneEffectRegistry} instance used for managing scene effects
   * in the rendering pipeline.
   *
   * @return the singleton {@link SceneEffectRegistry} instance containing registered scene effects
   */
  public static SceneEffectRegistry effects() {
    return EFFECTS;
  }

  /**
   * Checks whether any scene effect managed by the current rendering pipeline is enabled.
   *
   * @return true if at least one scene effect is enabled, false otherwise
   */
  public static boolean hasEnabledEffects() {
    return EFFECTS.hasEnabledEffects();
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
