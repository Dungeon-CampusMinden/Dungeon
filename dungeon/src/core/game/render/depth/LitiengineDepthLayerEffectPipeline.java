package core.game.render.depth;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

/**
 * Global depth-layer effect pipeline for the LITIENGINE backend.
 *
 * <p>Effects in this pipeline are applied to rendered entity layers grouped by
 * {@code DrawComponent.depth()} before those layers are composited into the world.
 */
public final class LitiengineDepthLayerEffectPipeline {

  private static final Map<Integer, LitiengineDepthLayerEffects> EFFECTS_BY_DEPTH = new TreeMap<>();

  private LitiengineDepthLayerEffectPipeline() {}

  public static LitiengineDepthLayerEffects effects(int depthLayer) {
    return EFFECTS_BY_DEPTH.computeIfAbsent(depthLayer, ignored -> new LitiengineDepthLayerEffects());
  }

  public static boolean hasEffects(int depthLayer) {
    LitiengineDepthLayerEffects effects = EFFECTS_BY_DEPTH.get(depthLayer);
    return effects != null && !effects.isEmpty();
  }

  public static boolean hasEnabledEffects(int depthLayer) {
    LitiengineDepthLayerEffects effects = EFFECTS_BY_DEPTH.get(depthLayer);
    return effects != null && effects.hasEnabledEffects();
  }

  public static boolean hasAnyEnabledEffects() {
    return EFFECTS_BY_DEPTH.values().stream().anyMatch(LitiengineDepthLayerEffects::hasEnabledEffects);
  }

  /**
   * @return true if all toggleable depth-layer effect groups are currently enabled
   */
  public static boolean allEnabled() {
    boolean hasToggleableDepthEffects = false;

    for (LitiengineDepthLayerEffects effects : EFFECTS_BY_DEPTH.values()) {
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
   * Returns whether all toggleable effects of one specific depth layer are currently enabled.
   *
   * @param depthLayer depth layer to inspect
   * @return true if the layer exists and all its toggleable effects are enabled
   */
  public static boolean allEnabled(int depthLayer) {
    LitiengineDepthLayerEffects effects = EFFECTS_BY_DEPTH.get(depthLayer);
    return effects != null && effects.allEnabled();
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
    LitiengineDepthLayerEffects effects = EFFECTS_BY_DEPTH.get(depthLayer);
    if (effects == null || effects.isEmpty()) {
      return false;
    }

    boolean newState = !effects.allEnabled();
    effects.enableAll(newState);
    return newState;
  }

  public static BufferedImage apply(int depthLayer, BufferedImage source, long nowMs) {
    LitiengineDepthLayerEffects effects = EFFECTS_BY_DEPTH.get(depthLayer);
    if (source == null || effects == null || !effects.hasEnabledEffects()) {
      return source;
    }

    BufferedImage current = source;
    for (LitiengineDepthLayerEffect effect : effects.getEnabledSorted()) {
      current = effect.apply(current, depthLayer, nowMs);
      if (current == null) {
        throw new IllegalStateException(
          effect.getClass().getSimpleName() + " returned null depth-layer image.");
      }
    }

    return current;
  }

  public static void clearDepth(int depthLayer) {
    EFFECTS_BY_DEPTH.remove(depthLayer);
  }

  public static void clear() {
    EFFECTS_BY_DEPTH.clear();
  }
}
