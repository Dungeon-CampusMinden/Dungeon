package core.platform.litiengine.render.depth;

import java.awt.image.BufferedImage;

/** Backend-specific post-processing effect for one rendered entity depth layer. */
public interface LitiengineDepthLayerEffect {

  /** @return true if the effect should currently be applied. */
  boolean enabled();

  /**
   * Applies the effect to the rendered image of a single entity depth layer.
   *
   * @param input rendered depth-layer image
   * @param depthLayer the depth layer this image belongs to
   * @param nowMs current timestamp in milliseconds
   * @return transformed depth-layer image, never {@code null}
   */
  BufferedImage apply(BufferedImage input, int depthLayer, long nowMs);
}
