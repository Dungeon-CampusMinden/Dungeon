package core.game.render.image;

import core.utils.components.draw.animation.AnimationFrame;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Utility class for converting animation frames to renderable images.
 *
 * <p>ImageFrameResolver provides static methods to extract and process animation frames from sprite
 * sheets and texture assets. It handles frame region extraction, horizontal flipping, and image
 * caching for performance optimization.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Converting AnimationFrame objects to BufferedImage instances
 *   <li>Loading textures from an asset management system
 *   <li>Extracting regions from sprite sheets
 *   <li>Applying frame transformations (horizontal flips)
 *   <li>Caching processed images for reuse
 * </ul>
 *
 * <p>This class is not instantiable; all methods are static utilities.
 */
public final class ImageFrameResolver {
  private ImageFrameResolver() {}

  /**
   * Converts an AnimationFrame to a BufferedImage, applying transformations as needed.
   *
   * <p>This method processes an animation frame by:
   *
   * <ol>
   *   <li>Checking the cached image (backend handle)
   *   <li>Loading the texture asset from the frame's path
   *   <li>Extracting the frame region if specified (for sprite sheets)
   *   <li>Applying horizontal flip transformation if indicated
   *   <li>Caching the result for future calls
   * </ol>
   *
   * <p>Defensive checks are performed to ensure region bounds are valid within the source image. If
   * any processing step fails (invalid region, missing texture, etc.), null is returned.
   *
   * @param frame the animation frame to convert (might be null)
   * @return the processed BufferedImage, or null if the frame is invalid or processing fails
   */
  public static BufferedImage toImage(final AnimationFrame frame) {
    if (frame == null) return null;

    // Cache hit
    final Object cached = frame.backendHandle();
    if (cached instanceof BufferedImage bi) {
      return bi;
    }

    final String raw = frame.texturePath() == null ? null : frame.texturePath().pathString();
    final String texturePath = ImageAssets.resolveImplicitFilePath(raw);
    if (texturePath == null || texturePath.isBlank()) return null;

    final BufferedImage base = ImageAssets.get(texturePath);
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
}
