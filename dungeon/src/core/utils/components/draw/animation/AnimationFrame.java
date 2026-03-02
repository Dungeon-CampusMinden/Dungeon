package core.utils.components.draw.animation;

import core.utils.components.path.IPath;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Engine-agnostic description of a single animation frame.
 *
 * <p>A frame references a texture path and optionally a rectangular region (spritesheet).
 * Backends (libGDX, LITIENGINE, ...) can resolve this into a concrete render object.
 */
public final class AnimationFrame implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final IPath texturePath;

  // Optional sub-region on a spritesheet. If regionW/regionH <= 0 -> full image.
  private final int regionX;
  private final int regionY;
  private final int regionW;
  private final int regionH;

  // Optional render hint (e.g. "mirror horizontally").
  private final boolean flipX;

  // Backend cache (e.g. libGDX Sprite/TextureRegion). Not serialized.
  private transient Object backendHandle;

  public static AnimationFrame fullImage(final IPath texturePath) {
    return fullImage(texturePath, false);
  }

  public static AnimationFrame fullImage(final IPath texturePath, final boolean flipX) {
    return new AnimationFrame(texturePath, -1, -1, -1, -1, flipX);
  }

  public static AnimationFrame region(final IPath texturePath, int x, int y, int w, int h) {
    return region(texturePath, x, y, w, h, false);
  }

  public static AnimationFrame region(
    final IPath texturePath, int x, int y, int w, int h, final boolean flipX) {
    return new AnimationFrame(texturePath, x, y, w, h, flipX);
  }

  public AnimationFrame(
    final IPath texturePath,
    final int regionX,
    final int regionY,
    final int regionW,
    final int regionH) {
    this(texturePath, regionX, regionY, regionW, regionH, false);
  }

  public AnimationFrame(
    final IPath texturePath,
    final int regionX,
    final int regionY,
    final int regionW,
    final int regionH,
    final boolean flipX) {
    this.texturePath = Objects.requireNonNull(texturePath, "texturePath");
    this.regionX = regionX;
    this.regionY = regionY;
    this.regionW = regionW;
    this.regionH = regionH;
    this.flipX = flipX;
  }

  public IPath texturePath() {
    return texturePath;
  }

  public boolean hasRegion() {
    return regionW > 0 && regionH > 0 && regionX >= 0 && regionY >= 0;
  }

  public int regionX() {
    return regionX;
  }

  public int regionY() {
    return regionY;
  }

  public int regionW() {
    return regionW;
  }

  public int regionH() {
    return regionH;
  }

  public boolean flipX() {
    return flipX;
  }

  public Object backendHandle() {
    return backendHandle;
  }

  public void backendHandle(final Object backendHandle) {
    this.backendHandle = backendHandle;
  }

  @Override
  public String toString() {
    return "AnimationFrame{"
      + "texturePath=" + texturePath
      + ", region=" + (hasRegion() ? (regionX + "," + regionY + "," + regionW + "," + regionH) : "<full>")
      + ", flipX=" + flipX
      + "}";
  }
}
