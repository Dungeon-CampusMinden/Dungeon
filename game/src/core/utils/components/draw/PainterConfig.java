package core.utils.components.draw;

import com.badlogic.gdx.graphics.Texture;
import core.utils.components.path.IPath;

/**
 * Configuration for the {@link Painter}
 *
 * <p>Each Texture needs its own configuration.
 *
 * @see Painter
 */
public final class PainterConfig {

  private final float xOffset;
  private final float yOffset;
  private final float xScaling;
  private final float yScaling;
  private int tintColor;

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

  private PainterConfig(
      float xOffset, float yOffset, float xScaling, float yScaling, int tintColor) {
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.xScaling = xScaling;
    this.yScaling = yScaling;
    this.tintColor = tintColor;
  }

  private PainterConfig(
      float xOffset, float yOffset, float xScaling, final Texture texture, int tintColor) {
    this(
        xOffset,
        yOffset,
        xScaling,
        ((float) texture.getHeight() / (float) texture.getWidth()),
        tintColor);
  }

  private PainterConfig(Texture texture) {
    this(0f, 0f, 1, texture, -1);
  }

  /**
   * Get the x-Offset in this configuration.
   *
   * @return x-Offset
   */
  public float xOffset() {
    return xOffset;
  }

  /**
   * Get the y-Offset in this configuration.
   *
   * @return y-Offset
   */
  public float yOffset() {
    return yOffset;
  }

  /**
   * Get the x-Scaling in this configuration.
   *
   * @return x-Scaling
   */
  public float xScaling() {
    return xScaling;
  }

  /**
   * Get the y-Scaling in this configuration.
   *
   * @return y-Scaling
   */
  public float yScaling() {
    return yScaling;
  }

  /**
   * Get the tint color in this configuration.
   *
   * @return tint color. Null if no tint color is set.
   */
  public int tintColor() {
    return tintColor;
  }

  /**
   * Set the tint color in this configuration.
   *
   * @param tintcolor The color to tint the texture with.
   */
  public void tintColor(int tintcolor) {
    this.tintColor = tintcolor;
  }
}
