package engine.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A system that draws world-space markers over the finished scene, such as attribute bars.
 *
 * <p>The draw system calls every registered system that implements this interface once per frame
 * after lighting, fog and bloom, also while a pausing dialog has stopped the systems. Overlays keep
 * their colours, scale with the zoom and land on the same texel grid as the sprites.
 */
public interface WorldOverlay {

  /** One art pixel in world units; offsets in whole art pixels keep overlays on the sprite grid. */
  float PIXEL = 1 / 16f;

  /**
   * Draws the overlay.
   *
   * @param batch batch with the world projection and premultiplied-alpha blending, already begun
   */
  void drawOverlay(SpriteBatch batch);

  /**
   * A single opaque white texel, to draw tinted rectangles with.
   *
   * @return the texture
   */
  static Texture pixel() {
    return RenderTextures.pixel();
  }
}
