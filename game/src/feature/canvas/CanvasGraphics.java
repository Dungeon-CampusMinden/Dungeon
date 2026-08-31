package feature.canvas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Small drawing helper for the canvas framework.
 *
 * <p>Provides a lazily created 1x1 white texture that is used to draw solid rectangles, grid lines
 * and outlines without depending on skin assets. The texture is shared and intentionally never
 * disposed: it is a single pixel that lives for the runtime of the game, just like the shared skin.
 */
public final class CanvasGraphics {

  private static TextureRegion white;

  private CanvasGraphics() {}

  /**
   * Returns the shared 1x1 white texture region.
   *
   * @return the white pixel region
   */
  public static TextureRegion white() {
    if (white == null) {
      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pixmap.setColor(Color.WHITE);
      pixmap.fill();
      Texture texture = new Texture(pixmap);
      pixmap.dispose();
      white = new TextureRegion(texture);
    }
    return white;
  }

  /**
   * Fills an axis aligned rectangle with the given color, honoring the batch alpha.
   *
   * @param batch the batch to draw with
   * @param color the fill color
   * @param parentAlpha the inherited alpha
   * @param x the left edge
   * @param y the bottom edge
   * @param width the rectangle width
   * @param height the rectangle height
   */
  public static void fill(
      Batch batch, Color color, float parentAlpha, float x, float y, float width, float height) {
    Color previous = batch.getColor().cpy();
    batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
    batch.draw(white(), x, y, width, height);
    batch.setColor(previous);
  }

  /**
   * Draws the outline of an axis aligned rectangle.
   *
   * <p>The outline is drawn inside the given bounds so it stays visible at the canvas edges.
   *
   * @param batch the batch to draw with
   * @param color the outline color
   * @param parentAlpha the inherited alpha
   * @param x the left edge
   * @param y the bottom edge
   * @param width the rectangle width
   * @param height the rectangle height
   * @param thickness the outline thickness
   */
  public static void outline(
      Batch batch,
      Color color,
      float parentAlpha,
      float x,
      float y,
      float width,
      float height,
      float thickness) {
    if (width <= 0f || height <= 0f) {
      return;
    }
    float t = Math.min(thickness, Math.min(width, height) / 2f);
    fill(batch, color, parentAlpha, x, y, width, t);
    fill(batch, color, parentAlpha, x, y + height - t, width, t);
    fill(batch, color, parentAlpha, x, y + t, t, height - 2f * t);
    fill(batch, color, parentAlpha, x + width - t, y + t, t, height - 2f * t);
  }
}
