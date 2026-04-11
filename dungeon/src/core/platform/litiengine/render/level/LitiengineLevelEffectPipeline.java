package core.platform.litiengine.render.level;

import java.awt.image.BufferedImage;

/**
 * Global level-pass effect pipeline for the LITIENGINE backend.
 *
 * <p>Effects in this pipeline are applied only to the rendered level layer before entities are
 * drawn on top.
 */
public final class LitiengineLevelEffectPipeline {

  private static final LitiengineLevelEffects EFFECTS = new LitiengineLevelEffects();

  private LitiengineLevelEffectPipeline() {}

  public static LitiengineLevelEffects effects() {
    return EFFECTS;
  }

  public static boolean hasEnabledEffects() {
    return EFFECTS.hasEnabledEffects();
  }

  public static boolean allEnabled() {
    return EFFECTS.allEnabled();
  }

  public static boolean toggleAll() {
    return EFFECTS.toggleAll();
  }

  public static BufferedImage apply(
    BufferedImage source, LitiengineLevelPassContext context, long nowMs) {
    if (source == null || !hasEnabledEffects()) {
      return source;
    }

    BufferedImage current = source;
    for (LitiengineLevelEffect effect : EFFECTS.getEnabledSorted()) {
      current = effect.apply(current, context, nowMs);
      if (current == null) {
        throw new IllegalStateException(
          effect.getClass().getSimpleName() + " returned null level image.");
      }
    }

    return current;
  }

  public static void clear() {
    EFFECTS.clear();
  }
}
