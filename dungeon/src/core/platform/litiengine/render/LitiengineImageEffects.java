package core.platform.litiengine.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small AWT-side image effect helpers for the LITIENGINE renderer.
 *
 * <p>These effects replace old libGDX shader semantics with CPU-side drawing.
 */
public final class LitiengineImageEffects {

  private static final Map<TintKey, BufferedImage> TINT_CACHE = new ConcurrentHashMap<>();

  private LitiengineImageEffects() {}

  public static void drawOutlinedSprite(
    final Graphics2D g,
    final BufferedImage sprite,
    final int x,
    final int y,
    final int width,
    final int height,
    final Color outlineColor,
    final int outlinePx) {

    if (g == null || sprite == null) {
      return;
    }

    if (outlinePx <= 0) {
      g.drawImage(sprite, x, y, width, height, null);
      return;
    }

    final BufferedImage tinted = tintedSilhouette(sprite, outlineColor);

    for (int dy = -outlinePx; dy <= outlinePx; dy++) {
      for (int dx = -outlinePx; dx <= outlinePx; dx++) {
        if (dx == 0 && dy == 0) {
          continue;
        }

        if (dx * dx + dy * dy > outlinePx * outlinePx) {
          continue;
        }

        g.drawImage(tinted, x + dx, y + dy, width, height, null);
      }
    }

    g.drawImage(sprite, x, y, width, height, null);
  }

  public static int effectiveOutlineWidth(
    final LitiengineOutlineEffectComponent effect, final long nowMs) {
    int baseWidth = Math.max(1, effect.width());

    if (effect.beatIntensity() <= 0f) {
      return baseWidth;
    }

    double phase = (nowMs / 1000.0) * effect.beatSpeed() * Math.PI * 2.0;
    double factor = 1.0 + Math.sin(phase) * effect.beatIntensity();
    return Math.max(1, (int) Math.round(baseWidth * factor));
  }

  public static Color effectiveOutlineColor(
    final LitiengineOutlineEffectComponent effect, final long nowMs) {
    if (!effect.rainbow()) {
      return effect.color();
    }

    float hue = (float) (((nowMs / 1000.0) * effect.beatSpeed()) % 1.0);
    Color rainbow = Color.getHSBColor(hue, 1.0f, 1.0f);
    return new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), effect.color().getAlpha());
  }

  private static BufferedImage tintedSilhouette(final BufferedImage source, final Color color) {
    final TintKey key =
      new TintKey(
        System.identityHashCode(source),
        source.getWidth(),
        source.getHeight(),
        color.getRGB());

    return TINT_CACHE.computeIfAbsent(key, ignored -> buildTintedSilhouette(source, color));
  }

  private static BufferedImage buildTintedSilhouette(
    final BufferedImage source, final Color color) {
    final BufferedImage tinted =
      new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

    final int rgb =
      ((color.getRed() & 0xFF) << 16)
        | ((color.getGreen() & 0xFF) << 8)
        | (color.getBlue() & 0xFF);

    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        int argb = source.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;

        if (alpha == 0) {
          tinted.setRGB(x, y, 0);
          continue;
        }

        int out = (alpha << 24) | rgb;
        tinted.setRGB(x, y, out);
      }
    }

    return tinted;
  }

  private record TintKey(int identityHash, int width, int height, int rgb) {}
}
