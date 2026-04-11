package core.platform.litiengine.render.depth;

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

  public static boolean hasEnabledEffects(int depthLayer) {
    LitiengineDepthLayerEffects effects = EFFECTS_BY_DEPTH.get(depthLayer);
    return effects != null && effects.hasEnabledEffects();
  }

  public static boolean hasAnyEnabledEffects() {
    return EFFECTS_BY_DEPTH.values().stream().anyMatch(LitiengineDepthLayerEffects::hasEnabledEffects);
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
