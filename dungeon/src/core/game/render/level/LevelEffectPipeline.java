package core.game.render.level;

import java.awt.image.BufferedImage;

/**
 * Global level-pass effect pipeline for the LITIENGINE backend.
 *
 * <p>Effects in this pipeline are applied only to the rendered level layer before entities are
 * drawn on top.
 */
public final class LevelEffectPipeline {

  private static final LevelEffectRegistry EFFECTS = new LevelEffectRegistry();

  private LevelEffectPipeline() {}

  public static LevelEffectRegistry effects() {
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
    BufferedImage source, LevelPassContext context, long nowMs) {
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

  public static void clear() {
    EFFECTS.clear();
  }
}
