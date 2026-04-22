package core.game.render.scene;

import java.awt.image.BufferedImage;

/**
 * Represents a scene effect that can be applied to a fully rendered scene image.
 *
 * <p>Scene effects are applied as post-processing operations on completed frames of the scene, often
 * to introduce visual enhancements or transformations. They can be selectively enabled or
 * disabled, and each effect should define its own application logic.
 */
public interface SceneEffect {

  /**
   * Checks whether this scene effect is currently enabled.
   *
   * @return true if the effect is enabled, false otherwise
   */
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
