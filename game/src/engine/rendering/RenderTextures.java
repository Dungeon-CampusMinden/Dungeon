package engine.rendering;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Procedurally generated helper textures of the atmosphere renderer. All textures are
 * premultiplied, like every texture loaded through {@link engine.utils.components.draw.TextureMap}.
 */
final class RenderTextures {

  private static Texture pixel;
  private static Texture blobShadow;
  private static Texture glow;
  private static Texture abyssFade;

  private RenderTextures() {}

  /**
   * A single opaque white texel, used for quads and particles.
   *
   * @return the texture
   */
  static Texture pixel() {
    if (pixel == null) {
      Pixmap p = newPixmap(1, 1);
      p.setColor(1, 1, 1, 1);
      p.fill();
      pixel = toTexture(p);
    }
    return pixel;
  }

  /**
   * A two-step pixel ellipse that grounds entities on the floor.
   *
   * @return the texture, 24x10 texels
   */
  static Texture blobShadow() {
    if (blobShadow == null) {
      int w = 24;
      int h = 10;
      Pixmap p = newPixmap(w, h);
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          float dx = (x + 0.5f - w / 2f) / (w / 2f);
          float dy = (y + 0.5f - h / 2f) / (h / 2f);
          float d = dx * dx + dy * dy;
          float a = d < 0.45f ? 0.62f : d < 1f ? 0.45f : 0f;
          p.setColor(0, 0, 0, a);
          p.drawPixel(x, y);
        }
      }
      blobShadow = toTexture(p);
    }
    return blobShadow;
  }

  /**
   * A soft white disc that fades out towards its rim, drawn additively around flames.
   *
   * @return the texture, 32x32 texels, linearly filtered
   */
  static Texture glow() {
    if (glow == null) {
      int size = 32;
      Pixmap p = newPixmap(size, size);
      for (int y = 0; y < size; y++) {
        for (int x = 0; x < size; x++) {
          float dx = (x + 0.5f - size / 2f) / (size / 2f);
          float dy = (y + 0.5f - size / 2f) / (size / 2f);
          float falloff = Math.max(0f, 1f - (float) Math.sqrt(dx * dx + dy * dy));
          float alpha = falloff * falloff;
          p.setColor(alpha, alpha, alpha, alpha);
          p.drawPixel(x, y);
        }
      }
      glow = toTexture(p);
      glow.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
    return glow;
  }

  /**
   * Darkness over the side of a floor that drops into the void: already shaded at its top edge, so
   * the side reads apart from the floor above, and black at its bottom.
   *
   * @return the texture, 16x16 texels
   */
  static Texture abyssFade() {
    if (abyssFade == null) {
      Pixmap p = newPixmap(16, 16);
      for (int y = 0; y < 16; y++) {
        float depth = y / 15f;
        p.setColor(0, 0, 0, Math.min(1f, 0.45f + 0.75f * (float) Math.pow(depth, 0.8)));
        p.drawLine(0, y, 15, y);
      }
      abyssFade = toTexture(p);
    }
    return abyssFade;
  }

  private static Pixmap newPixmap(int width, int height) {
    Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    p.setBlending(Pixmap.Blending.None);
    return p;
  }

  private static Texture toTexture(Pixmap p) {
    Texture t = new Texture(p);
    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    p.dispose();
    return t;
  }
}
