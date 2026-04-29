package core.game.render.scene;

import java.awt.image.BufferedImage;

/**
 * Utility class for managing and applying scene effects in a rendering pipeline.
 *
 * <p>This class provides a centralized mechanism to register, toggle, and apply post-processing
 * effects to rendered scene images. Scene effects are prioritized and applied sequentially in the
 * order defined by their priority and insertion sequence in the {@link SceneEffectRegistry}.
 *
 * <p>The {@code SceneEffectPipeline} is a global accessor to operations such as toggling all
 * effects, applying enabled effects, and managing the effect registry. It primarily interacts with
 * {@link SceneEffectRegistry} and ensures that all operations are consistent with the underlying
 * registry.
 *
 * <p>This class is not intended to be instantiated and contains only static utility methods.
 */
public final class SceneEffectPipeline {

  private static final SceneEffectRegistry EFFECTS = new SceneEffectRegistry();

  private SceneEffectPipeline() {}

  /**
   * Retrieves the global {@link SceneEffectRegistry} instance used for managing scene effects in
   * the rendering pipeline.
   *
   * @return the singleton {@link SceneEffectRegistry} instance containing registered scene effects
   */
  public static SceneEffectRegistry effects() {
    return EFFECTS;
  }

   /**
    * Checks whether no scene effects managed by the current rendering pipeline are enabled.
    *
    * @return true if all scene effects are disabled, false otherwise
    */
   public static boolean hasNoEnabledEffects() {
     return EFFECTS.hasNoEnabledEffects();
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
    * @return processed frame image
    */
    public static BufferedImage apply(BufferedImage source) {
      if (source == null || hasNoEnabledEffects()) {
        return source;
      }

     BufferedImage current = source;
     for (SceneEffect effect : EFFECTS.getEnabledSorted()) {
       current = effect.apply(current);
       if (current == null) {
         throw new IllegalStateException(
             effect.getClass().getSimpleName() + " returned null scene image.");
       }
     }

     return current;
   }
}
