package core.utils.components.draw;

import com.badlogic.gdx.graphics.Texture;
import core.components.DrawComponent;
import core.utils.Vector2;
import core.utils.components.path.IPath;

/**
 * Configuration for the {@link Painter}
 *
 * <p>Each Texture needs its own configuration.
 *
 * @see Painter
 */
public final class PainterConfig {

  private final Vector2 offset;
  private final Vector2 size;
  private Vector2 scale = Vector2.ONE;
  private int tintColor = -1; // -1 means no tint color
  private boolean mirrored = false;
  private float rotation = 0f;

  /**
   * Create a new PainterConfig with the given offset.
   *
   * <p>Scaling based on the given texture.
   *
   * @param texturePath Path to the texture.
   * @param xOffset The texture will be moved on the x-axis, based on this value.
   * @param yOffset The texture will be moved on the y-axis, based on this value.
   * @param tintColor The color to tint the texture with.
   */
  public PainterConfig(final IPath texturePath, float xOffset, float yOffset, int tintColor) {
    // half the texture xOffset, yOffset is a quarter texture down
    this(xOffset, yOffset, 1, TextureMap.instance().textureAt(texturePath), tintColor);
  }

  /**
   * Create a new PainterConfig with the given offset.
   *
   * <p>Scaling and offsets based on the given texture.
   *
   * @param texturePath Path to the texture.
   */
  public PainterConfig(final IPath texturePath) {
    this(TextureMap.instance().textureAt(texturePath));
  }

  /**
   * Creates a painter config with the given offset, size and tint.
   *
   * @param xOffset The x offset
   * @param yOffset The y offset
   * @param xSize The x scaling
   * @param ySize The y scaling
   * @param tintColor The tint color as used by the {@link DrawComponent}
   */
  public PainterConfig(float xOffset, float yOffset, float xSize, float ySize, int tintColor) {
    this.offset = Vector2.of(xOffset, yOffset);
    this.size = Vector2.of(xSize, ySize);
    this.tintColor = tintColor;
  }

  private PainterConfig(
      float xOffset, float yOffset, float xSize, final Texture texture, int tintColor) {
    this(
        xOffset,
        yOffset,
        xSize,
        ((float) texture.getHeight() / (float) texture.getWidth()),
        tintColor);
  }

  private PainterConfig(Texture texture) {
    this(0f, 0f, 1, texture, -1);
  }

  /**
   * Get the offset in this configuration.
   *
   * @return offset as a {@link Vector2}
   */
  public Vector2 offset() {
    return offset;
  }

  /**
   * Get the scaling in this configuration.
   *
   * @return scaling as a {@link Vector2}
   */
  public Vector2 size() {
    return size;
  }

  /**
   * Get the scale in this configuration.
   *
   * @return scale as a {@link Vector2}
   */
  public Vector2 scale() {
    return scale;
  }

  /**
   * Set the scale in this configuration.
   *
   * @param scale The scale as a {@link Vector2}
   */
  public void scale(Vector2 scale) {
    this.scale = scale;
  }

  /**
   * Get the tint color in this configuration.
   *
   * @return The color to tint the texture with. -1 means no tint color.
   */
  public int tintColor() {
    return this.tintColor;
  }

  /**
   * Set the tint color in this configuration.
   *
   * @param tintcolor The color to tint the texture with.
   */
  public void tintColor(int tintcolor) {
    this.tintColor = tintcolor;
  }

  /**
   * Get whether the texture should be mirrored.
   *
   * @return true if the texture should be mirrored, false otherwise.
   */
  public boolean mirrored() {
    return mirrored;
  }

  /**
   * Set whether the texture should be mirrored.
   *
   * @param mirrored true if the texture should be mirrored, false otherwise.
   */
  public void mirrored(boolean mirrored) {
    this.mirrored = mirrored;
  }

  /**
   * Get the rotation of the texture in degrees.
   *
   * @return rotation in degrees.
   */
  public float rotation() {
    return rotation;
  }

  /**
   * Set the rotation of the texture in degrees.
   *
   * @param rotation rotation in degrees.
   */
  public void rotation(float rotation) {
    this.rotation = rotation;
  }
}
