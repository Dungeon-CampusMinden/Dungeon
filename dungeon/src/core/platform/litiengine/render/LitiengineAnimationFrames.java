package core.platform.litiengine.render;

import core.utils.components.draw.animation.AnimationFrame;
import de.gurkenlabs.litiengine.resources.Resources;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Utility to resolve engine-agnostic {@link AnimationFrame}s into LITIENGINE/AWT {@link BufferedImage}s.
 *
 * <p>Caches the resolved image in {@link AnimationFrame#backendHandle()} to avoid repeated work.
 */
public final class LitiengineAnimationFrames {
  private LitiengineAnimationFrames() {}

  public static BufferedImage toImage(final AnimationFrame frame) {
    if (frame == null) return null;

    // Cache hit
    final Object cached = frame.backendHandle();
    if (cached instanceof BufferedImage bi) {
      return bi;
    }

    final String raw = frame.texturePath() == null ? null : frame.texturePath().pathString();
    if (raw == null || raw.isBlank()) return null;

    final String resolved = resolveImplicitFilePath(raw);

    // Let LITIENGINE handle its internal resource cache.
    final BufferedImage base = Resources.images().get(resolved);
    if (base == null) return null;

    BufferedImage out = base;

    // Apply region (spritesheet)
    if (frame.hasRegion()) {
      final int x = frame.regionX();
      final int y = frame.regionY();
      final int w = frame.regionW();
      final int h = frame.regionH();

      if (x < 0 || y < 0 || w <= 0 || h <= 0) {
        return null;
      }
      if (x + w > base.getWidth() || y + h > base.getHeight()) {
        // Defensive: avoid RasterFormatException if config is wrong.
        return null;
      }

      out = base.getSubimage(x, y, w, h);
    }

    // Apply flip hint
    if (frame.flipX()) {
      out = flipHorizontally(out);
    }

    frame.backendHandle(out);
    return out;
  }

  private static BufferedImage flipHorizontally(final BufferedImage src) {
    if (src == null) return null;

    final BufferedImage dst =
      new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);

    final Graphics2D g = dst.createGraphics();
    try {
      final AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
      tx.translate(-src.getWidth(), 0);
      g.drawImage(src, tx, null);
      return dst;
    } finally {
      g.dispose();
    }
  }

  /**
   * Matches the "folder name implies png" convention (same as in Animation/Texture helpers):
   * - "character/wizard/" -> "character/wizard/wizard.png"
   * - "character/wizard"  -> "character/wizard/wizard.png"
   * - "foo.png"           -> "foo.png"
   */
  private static String resolveImplicitFilePath(String pathString) {
    if (pathString == null || pathString.isEmpty()) return pathString;

    // Already explicit image file
    if (pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      return pathString;
    }

    // Folder or implicit base name
    String dir = pathString.replaceAll("/$", "");
    String baseName = dir.substring(dir.lastIndexOf('/') + 1);
    return dir + "/" + baseName + ".png";
  }
}
