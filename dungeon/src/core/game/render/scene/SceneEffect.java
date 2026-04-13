package core.game.render.scene;

import java.awt.image.BufferedImage;

/** Backend-specific post-processing effect for the fully rendered LITIENGINE scene. */
public interface SceneEffect {

  /** @return true if the effect should currently be applied. */
  boolean enabled();

  /**
   * Applies the effect to the fully rendered scene image.
   *
   * @param input fully rendered frame image
   * @param nowMs current timestamp in milliseconds
   * @return transformed scene image, never {@code null}
   */
  BufferedImage apply(BufferedImage input, long nowMs);
}
