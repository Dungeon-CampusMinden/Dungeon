package core.game.render.level;

import java.awt.image.BufferedImage;

/** Post-processing effect for the rendered level layer. */
public interface LevelEffect {

  /**
   * Indicates whether this level effect is currently enabled.
   *
   * @return true if the effect is enabled; false otherwise
   */
  boolean enabled();

  /**
   * Applies the effect to the fully rendered level-layer image.
   *
   * @param input rendered level-layer image
   * @param context world-space metadata for the rendered visible level buffer
   * @param nowMs current timestamp in milliseconds
   * @return transformed level-layer image, never {@code null}
   */
  BufferedImage apply(BufferedImage input, LevelPassContext context, long nowMs);
}
