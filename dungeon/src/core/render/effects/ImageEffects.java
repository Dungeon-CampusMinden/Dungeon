package core.render.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for rendering image effects and visual enhancements.
 *
 * <p>ImageEffects provides static methods for applying visual effects to sprites and images,
 * such as outlines, tinting, and color animations. It includes caching mechanisms for performance
 * optimization when rendering frequently used effects.
 *
 * <p>Key features:
 * <ul>
 *   <li>Drawing sprites with customizable outline effects
 *   <li>Computing dynamic outline widths with pulsing/beating effects
 *   <li>Computing dynamic outline colors with rainbow animation support
 *   <li>Caching tinted silhouettes for performance
 * </ul>
 *
 * <p>This class uses a thread-safe cache to store computed tinted silhouettes, reducing the
 * computational overhead of repeatedly generating the same tinted images.
 */
public final class ImageEffects {

  private static final Map<TintKey, BufferedImage> TINT_CACHE = new ConcurrentHashMap<>();

  private ImageEffects() {}

  /**
   * Draws a sprite with an optional outline effect on the given graphics context.
   *
   * <p>This method renders the sprite at the specified position and size, with an outline of the
   * given color and thickness. The outline is created by drawing multiple offset copies of a
   * tinted silhouette behind the original sprite.
   *
   * <p>If the outline width is 0 or less, the sprite is drawn without an outline. If either
   * graphics context or sprite is null, the method returns without drawing anything.
   *
   * @param g the Graphics2D context to draw on
   * @param sprite the sprite image to draw
   * @param x the x-coordinate for drawing
   * @param y the y-coordinate for drawing
   * @param width the width to scale the sprite to
   * @param height the height to scale the sprite to
   * @param outlineColor the color of the outline effect
   * @param outlinePx the thickness of the outline in pixels
   */
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

  /**
   * Computes the effective outline width based on the effect configuration and current time.
   *
   * <p>If beat intensity is set, the outline width oscillates sinusoidally with time, creating
   * a pulsing effect. The oscillation frequency is controlled by the beat speed parameter.
   *
   * @param effect the outline effect component defining width and beat parameters
   * @param nowMs the current time in milliseconds since epoch
   * @return the effective outline width in pixels (minimum 1)
   */
  public static int effectiveOutlineWidth(
    final OutlineEffectComponent effect, final long nowMs) {
    int baseWidth = Math.max(1, effect.width());

    if (effect.beatIntensity() <= 0f) {
      return baseWidth;
    }

    double phase = (nowMs / 1000.0) * effect.beatSpeed() * Math.PI * 2.0;
    double factor = 1.0 + Math.sin(phase) * effect.beatIntensity();
    return Math.max(1, (int) Math.round(baseWidth * factor));
  }

  /**
   * Computes the effective outline color based on the effect configuration and current time.
   *
   * <p>If rainbow mode is enabled, the color cycles through the hue spectrum at a rate controlled
   * by the beat speed parameter. The alpha channel from the effect's base color is preserved.
   *
   * <p>If rainbow mode is disabled, the effect's configured color is returned unchanged.
   *
   * @param effect the outline effect component defining color and animation parameters
   * @param nowMs the current time in milliseconds since epoch
   * @return the effective outline color, either static or animated based on rainbow mode
   */
  public static Color effectiveOutlineColor(
    final OutlineEffectComponent effect, final long nowMs) {
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
