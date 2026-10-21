package engine.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import engine.utils.components.draw.TextureMap;
import java.util.BitSet;
import java.util.Optional;

/**
 * Finds where a sprite frame actually touches the ground: its lowest opaque pixel row as displayed
 * and the horizontal extent of the opaque pixels just above it. Contact shadows use it to sit under
 * feet, legs and table legs instead of under the transparent sprite box.
 *
 * <p>The opaque pixels come from {@link TextureMap#opaquePixels(Texture)}, recorded when the
 * texture was loaded, so a frame costs a scan of its lowest rows and nothing is cached here.
 */
final class SpriteFootprints {

  /**
   * The visible base of a frame as displayed, as fractions of the frame size.
   *
   * @param left left edge of the base, 0 = left frame edge
   * @param right right edge of the base, 1 = right frame edge
   * @param bottom height of the lowest opaque row above the frame's bottom edge
   */
  record Footprint(float left, float right, float bottom) {}

  // Rows above the lowest opaque one that still count as the base (feet, legs).
  private static final int BASE_ROWS = 3;

  private SpriteFootprints() {}

  /**
   * Returns the footprint of a frame, honouring the frame's own horizontal and vertical flips.
   *
   * @param region the frame
   * @return the footprint, or empty if the texture's pixels are unknown or the frame is empty
   */
  static Optional<Footprint> of(TextureRegion region) {
    Texture texture = region.getTexture();
    Optional<BitSet> mask = TextureMap.instance().opaquePixels(texture);
    if (mask.isEmpty()) return Optional.empty();
    BitSet opaque = mask.get();
    int textureWidth = texture.getWidth();
    int textureHeight = texture.getHeight();

    // Flipped regions swap their UV endpoints; the pixel rectangle starts at the smaller one.
    boolean flipX = region.getU() > region.getU2();
    boolean flipY = region.getV() > region.getV2();
    int x0 = Math.round(Math.min(region.getU(), region.getU2()) * textureWidth);
    int y0 = Math.round(Math.min(region.getV(), region.getV2()) * textureHeight);
    int w = Math.min(Math.abs(region.getRegionWidth()), textureWidth - x0);
    int h = Math.min(Math.abs(region.getRegionHeight()), textureHeight - y0);
    if (w <= 0 || h <= 0) return Optional.empty();

    // Step i is the i-th row above the displayed bottom edge; image rows run top to bottom.
    int lowest = -1;
    for (int i = 0; i < h && lowest < 0; i++) {
      if (firstOpaque(opaque, textureWidth, x0, w, row(y0, h, i, flipY)) >= 0) lowest = i;
    }
    if (lowest < 0) return Optional.empty();

    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    for (int i = lowest; i < Math.min(h, lowest + BASE_ROWS); i++) {
      int offset = row(y0, h, i, flipY) * textureWidth;
      for (int x = x0; x < x0 + w; x++) {
        if (opaque.get(offset + x)) {
          minX = Math.min(minX, x);
          maxX = Math.max(maxX, x);
        }
      }
    }
    float left = (minX - x0) / (float) w;
    float right = (maxX + 1 - x0) / (float) w;
    if (flipX) {
      float mirroredLeft = 1 - right;
      right = 1 - left;
      left = mirroredLeft;
    }
    return Optional.of(new Footprint(left, right, lowest / (float) h));
  }

  private static int row(int y0, int h, int stepsAboveBottom, boolean flipY) {
    return flipY ? y0 + stepsAboveBottom : y0 + h - 1 - stepsAboveBottom;
  }

  private static int firstOpaque(BitSet opaque, int textureWidth, int x0, int w, int row) {
    int start = row * textureWidth + x0;
    int hit = opaque.nextSetBit(start);
    return hit >= 0 && hit < start + w ? hit : -1;
  }
}
