package core.utils.components.draw.animation;

import core.utils.components.path.IPath;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single frame of an animation.
 *
 * <p>AnimationFrame encapsulates all data needed to render one frame of an animation, including
 * the texture/image source, optional region information for sprite sheets, and horizontal flip state.
 *
 * <p>A frame can reference either:
 * <ul>
 *   <li>A full image (entire texture file as one frame)
 *   <li>A region of a sprite sheet (defined by x, y, width, height coordinates)
 * </ul>
 *
 * <p>Frames support horizontal flipping/mirroring for efficient animation variation without
 * duplicating image assets. The backend handle provides an internal cache for rendering
 * backends to store compiled/processed versions of the frame.
 *
 * <p>This class is serializable for persistence in animation data structures.
 */
public final class AnimationFrame implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final IPath texturePath;

  private final int regionX;
  private final int regionY;
  private final int regionW;
  private final int regionH;

  private final boolean flipX;

  private transient Object backendHandle;

  /**
   * Factory method to create a full-image animation frame without flipping.
   *
   * <p>The resulting frame will reference the entire image file as a single frame.
   *
   * @param texturePath the path to the texture/image file
   * @return a new AnimationFrame referencing the full image
   */
  public static AnimationFrame fullImage(final IPath texturePath) {
    return fullImage(texturePath, false);
  }

  /**
   * Factory method to create a full-image animation frame with optional horizontal flipping.
   *
   * <p>The resulting frame will reference the entire image file as a single frame.
   *
   * @param texturePath the path to the texture/image file
   * @param flipX whether to flip the frame horizontally
   * @return a new AnimationFrame referencing the full image
   */
  public static AnimationFrame fullImage(final IPath texturePath, final boolean flipX) {
    return new AnimationFrame(texturePath, -1, -1, -1, -1, flipX);
  }

  /**
   * Factory method to create a sprite sheet region frame without flipping.
   *
   * <p>The resulting frame will reference a specific rectangular region within a larger sprite sheet.
   *
   * @param texturePath the path to the sprite sheet image
   * @param x the x-coordinate (in pixels) of the region within the sprite sheet
   * @param y the y-coordinate (in pixels) of the region within the sprite sheet
   * @param w the width (in pixels) of the region
   * @param h the height (in pixels) of the region
   * @return a new AnimationFrame referencing the specified region
   */
  public static AnimationFrame region(final IPath texturePath, int x, int y, int w, int h) {
    return region(texturePath, x, y, w, h, false);
  }

  /**
   * Factory method to create a sprite sheet region frame with optional horizontal flipping.
   *
   * <p>The resulting frame will reference a specific rectangular region within a larger sprite sheet.
   *
   * @param texturePath the path to the sprite sheet image
   * @param x the x-coordinate (in pixels) of the region within the sprite sheet
   * @param y the y-coordinate (in pixels) of the region within the sprite sheet
   * @param w the width (in pixels) of the region
   * @param h the height (in pixels) of the region
   * @param flipX whether to flip the frame horizontally
   * @return a new AnimationFrame referencing the specified region
   */
  public static AnimationFrame region(
    final IPath texturePath, int x, int y, int w, int h, final boolean flipX) {
    return new AnimationFrame(texturePath, x, y, w, h, flipX);
  }

  /**
   * Constructs an AnimationFrame without horizontal flipping.
   *
   * <p>The region parameters define a sprite sheet region. Use negative values for all region
   * parameters to create a full-image frame instead.
   *
   * @param texturePath the path to the texture/image file (must not be null)
   * @param regionX the x-coordinate of the region (use -1 for full-image frames)
   * @param regionY the y-coordinate of the region (use -1 for full-image frames)
   * @param regionW the width of the region (use -1 for full-image frames)
   * @param regionH the height of the region (use -1 for full-image frames)
   * @throws NullPointerException if texturePath is null
   */
  public AnimationFrame(
    final IPath texturePath,
    final int regionX,
    final int regionY,
    final int regionW,
    final int regionH) {
    this(texturePath, regionX, regionY, regionW, regionH, false);
  }

  /**
   * Constructs an AnimationFrame with optional horizontal flipping.
   *
   * <p>The region parameters define a sprite sheet region. Use negative values for all region
   * parameters to create a full-image frame instead.
   *
   * @param texturePath the path to the texture/image file (must not be null)
   * @param regionX the x-coordinate of the region (use -1 for full-image frames)
   * @param regionY the y-coordinate of the region (use -1 for full-image frames)
   * @param regionW the width of the region (use -1 for full-image frames)
   * @param regionH the height of the region (use -1 for full-image frames)
   * @param flipX whether to flip the frame horizontally
   * @throws NullPointerException if texturePath is null
   */
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

  /**
   * Gets the texture/image path for this frame.
   *
   * @return the path to the texture or sprite sheet
   */
  public IPath texturePath() {
    return texturePath;
  }

  /**
   * Checks whether this frame references a sprite sheet region.
   *
   * <p>Returns true if the frame has valid region dimensions; false for full-image frames.
   *
   * @return true if this frame uses a region, false if it references a full image
   */
  public boolean hasRegion() {
    return regionW > 0 && regionH > 0 && regionX >= 0 && regionY >= 0;
  }

  /**
   * Gets the x-coordinate of the sprite sheet region.
   *
   * @return the x-coordinate in pixels, or -1 for full-image frames
   */
  public int regionX() {
    return regionX;
  }

  /**
   * Gets the y-coordinate of the sprite sheet region.
   *
   * @return the y-coordinate in pixels, or -1 for full-image frames
   */
  public int regionY() {
    return regionY;
  }

  /**
   * Gets the width of the sprite sheet region.
   *
   * @return the width in pixels, or -1 for full-image frames
   */
  public int regionW() {
    return regionW;
  }

  /**
   * Gets the height of the sprite sheet region.
   *
   * @return the height in pixels, or -1 for full-image frames
   */
  public int regionH() {
    return regionH;
  }

  /**
   * Checks whether this frame is horizontally flipped.
   *
   * @return true if the frame is flipped horizontally, false otherwise
   */
  public boolean flipX() {
    return flipX;
  }

  /**
   * Gets the backend-specific cached handle for this frame.
   *
   * <p>The backend handle is used by rendering systems to store compiled or cached versions
   * of this frame (e.g., compiled GPU textures). It is transient and not serialized.
   *
   * @return the backend handle, or null if not set
   */
  public Object backendHandle() {
    return backendHandle;
  }

  /**
   * Sets the backend-specific cached handle for this frame.
   *
   * <p>The backend handle is used by rendering systems to store compiled or cached versions
   * of this frame (e.g., compiled GPU textures). It is transient and not serialized.
   *
   * @param backendHandle the backend handle object, typically a compiled texture or similar resource
   */
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
