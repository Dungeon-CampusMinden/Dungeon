package core.game.render.sprite.effects;

import java.awt.image.BufferedImage;

/** Backend-specific sprite effect for the LITIENGINE Graphics2D render path. */
public interface SpriteEffect {

  /** @return true if the effect should currently be applied. */
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
