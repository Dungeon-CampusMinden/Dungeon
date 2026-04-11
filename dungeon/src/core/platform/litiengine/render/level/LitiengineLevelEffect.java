package core.platform.litiengine.render.level;

import java.awt.image.BufferedImage;

/** Backend-specific post-processing effect for the rendered level layer only. */
public interface LitiengineLevelEffect {

  /** @return true if the effect should currently be applied. */
  boolean enabled();

  /**
   * Applies the effect to the fully rendered level-layer image.
   *
   * @param input rendered level-layer image
   * @param context world-space metadata for the rendered visible level buffer
   * @param nowMs current timestamp in milliseconds
   * @return transformed level-layer image, never {@code null}
   */
  BufferedImage apply(BufferedImage input, LitiengineLevelPassContext context, long nowMs);
}
