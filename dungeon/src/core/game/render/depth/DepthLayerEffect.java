package core.game.render.depth;

import java.awt.image.BufferedImage;

/** Backend-specific post-processing effect for one rendered entity depth layer. */
public interface DepthLayerEffect {

  /**
   * Indicates whether the effect is currently enabled.
   *
   * @return true if the effect is enabled, false otherwise
   */
  boolean enabled();

  /**
   * Applies the effect to the rendered image of a single entity depth layer.
   *
   * @param input rendered depth-layer image
   * @return transformed depth-layer image, never {@code null}
   */
  BufferedImage apply(BufferedImage input);
}
