package core.game.render.depth;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

/**
 * A pipeline for applying effects to individual entity depth layers.
 *
 * <p>This class manages effect registries for each depth layer independently, allowing different
 * visual effects to be applied to different entity layers.
 *
 * <p>Effects are applied sequentially to each depth layer's rendered entities.
 */
public final class DepthLayerEffectPipeline {

  private static final Map<Integer, DepthLayerEffectRegistry> EFFECTS_BY_DEPTH = new TreeMap<>();

  private DepthLayerEffectPipeline() {}

  /**
   * Gets the effect registry for the specified depth layer, creating it if necessary.
   *
   * @param depthLayer the depth layer identifier
   * @return the effect registry for the depth layer
   */
  public static DepthLayerEffectRegistry effects(int depthLayer) {
    return EFFECTS_BY_DEPTH.computeIfAbsent(depthLayer, ignored -> new DepthLayerEffectRegistry());
  }

  /**
   * Checks whether any effects (enabled or disabled) are registered for the specified depth layer.
   *
   * @param depthLayer the depth layer to check
   * @return true if effects exist for this depth layer, false otherwise
   */
  public static boolean hasEffects(int depthLayer) {
    DepthLayerEffectRegistry effects = EFFECTS_BY_DEPTH.get(depthLayer);
    return effects != null && !effects.isEmpty();
  }

  /**
    * Checks whether no enabled effects are registered for the specified depth layer.
    *
    * @param depthLayer the depth layer to check
    * @return true if all effects are disabled for this depth layer, false otherwise
    */
   public static boolean hasNoEnabledEffects(int depthLayer) {
     DepthLayerEffectRegistry effects = EFFECTS_BY_DEPTH.get(depthLayer);
     return effects == null || effects.hasNoEnabledEffects();
   }

  /**
   * Checks whether all toggleable effects in all depth layers are enabled.
   *
   * @return true if all registered toggleable effect groups are enabled
   */
  public static boolean allEnabled() {
    boolean hasToggleableDepthEffects = false;

    for (DepthLayerEffectRegistry effects : EFFECTS_BY_DEPTH.values()) {
      if (!effects.isEmpty()) {
        hasToggleableDepthEffects = true;
        if (!effects.allEnabled()) {
          return false;
        }
      }
    }

    return hasToggleableDepthEffects;
  }

  /**
   * Toggles all registered toggleable depth-layer effect groups at once.
   *
   * @return the new enabled state that was applied
   */
  public static boolean toggleAll() {
    boolean newState = !allEnabled();
    EFFECTS_BY_DEPTH.values().forEach(effects -> effects.enableAll(newState));
    return newState;
  }

  /**
   * Toggles all registered toggleable effects of one specific depth layer at once.
   *
   * @param depthLayer depth layer to toggle
   * @return the new enabled state that was applied
   */
  public static boolean toggleAll(int depthLayer) {
    DepthLayerEffectRegistry effects = EFFECTS_BY_DEPTH.get(depthLayer);
    if (effects == null || effects.isEmpty()) {
      return false;
    }

    boolean newState = !effects.allEnabled();
    effects.enableAll(newState);
    return newState;
  }

  /**
   * Applies all enabled depth layer effects to the provided image for the specified depth layer.
   *
   * @param depthLayer the depth layer to apply effects for
   * @param source the source image to apply effects to (can be null)
   * @return the processed image with all effects applied, or the original image if no effects are
   *     enabled
   * @throws IllegalStateException if an effect returns null
   */
   public static BufferedImage apply(int depthLayer, BufferedImage source) {
     DepthLayerEffectRegistry effects = EFFECTS_BY_DEPTH.get(depthLayer);
     if (source == null || hasNoEnabledEffects(depthLayer)) {
       return source;
     }

     BufferedImage current = source;
     for (DepthLayerEffect effect : effects.getEnabledSorted()) {
       current = effect.apply(current);
       if (current == null) {
         throw new IllegalStateException(
             effect.getClass().getSimpleName() + " returned null depth-layer image.");
       }
     }

    return current;
  }
}
