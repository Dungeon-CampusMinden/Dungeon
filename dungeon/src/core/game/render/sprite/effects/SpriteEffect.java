package core.game.render.sprite.effects;

import java.awt.image.BufferedImage;

/**
 * Represents a sprite effect that can be applied to a sprite image. Implementations of this
 * interface can define various transformations, such as color grading, blurring, or other visual
 * effects.
 *
 * <p>The effect is applied only if it is enabled, allowing for dynamic control over the
 * visual appearance of sprites in the game.
 *
 * <p>Implementations should ensure that the {@code apply} method returns a non-null image, even
 * if the effect does not modify the input image.
 */
public interface SpriteEffect {

  /**
   * Determines whether the effect is currently enabled and should be applied to sprites.
   *
   * @return {@code true} if the effect is enabled and should be applied; {@code false} otherwise
   */
  boolean enabled();

  /**
   * Applies the effect to the given sprite image.
   *
   * @param input source sprite image
   * @param nowMs current timestamp in milliseconds
   * @return transformed sprite image, never {@code null}
   */
  BufferedImage apply(BufferedImage input, long nowMs);
}
