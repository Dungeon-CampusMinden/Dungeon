package core.utils;

import com.badlogic.gdx.graphics.Color;

/**
 * Internal record used to cache fonts based on their unique configuration.
 *
 * @param path the font file path
 * @param size the font size
 * @param color the font color
 * @param borderWidth the border width
 * @param borderColor the border color
 */
public record FontSpec(String path, int size, Color color, float borderWidth, Color borderColor) {

  /**
   * Creates a new FontSpec with the same parameters but a different size.
   *
   * @param size the new font size
   * @return a new FontSpec instance with the updated size
   */
  public FontSpec withSize(int size) {
    return new FontSpec(this.path, size, this.color, this.borderWidth, this.borderColor);
  }

  /**
   * Creates a new FontSpec with the same parameters but a different color.
   *
   * @param color the new font color
   * @return a new FontSpec instance with the updated color
   */
  public FontSpec withColor(Color color) {
    return new FontSpec(this.path, this.size, color, this.borderWidth, this.borderColor);
  }

  /**
   * Creates a new FontSpec with the same parameters but different border settings.
   *
   * @param borderWidth the new border width in pixels
   * @param borderColor the new border color
   * @return a new FontSpec instance with the updated border settings
   */
  public FontSpec withBorder(float borderWidth, Color borderColor) {
    return new FontSpec(this.path, this.size, this.color, borderWidth, borderColor);
  }

  /**
   * Factory method to create a FontSpec with full customization options.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @param color the font color
   * @param borderWidth the border width in pixels
   * @param borderColor the color of the border
   * @return a new FontSpec instance with the specified parameters
   */
  public static FontSpec of(
      String path, int size, Color color, float borderWidth, Color borderColor) {
    return new FontSpec(path, size, color, borderWidth, borderColor);
  }

  /**
   * Factory method to create a FontSpec with a specific path, size, and color.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @param color the font color
   * @return a new FontSpec instance with no border
   */
  public static FontSpec of(String path, int size, Color color) {
    return new FontSpec(path, size, color, 0, Color.BLACK);
  }

  /**
   * Factory method to create a FontSpec with a specific path and size.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @return a new FontSpec instance with white color and no border
   */
  public static FontSpec of(String path, int size) {
    return new FontSpec(path, size, Color.WHITE, 0, Color.BLACK);
  }

  /**
   * Factory method to create a FontSpec with a specific size using the default font path.
   *
   * @param size the size of the font
   * @return a new FontSpec instance with default path, white color, and no border
   */
  public static FontSpec of(int size) {
    return new FontSpec(FontHelper.DEFAULT_FONT_PATH, size, Color.WHITE, 0, Color.BLACK);
  }

  /**
   * Factory method to create a FontSpec with a specific size and color using the default font path.
   *
   * @param size the size of the font
   * @param color the font color
   * @return a new FontSpec instance with default path and no border
   */
  public static FontSpec of(int size, Color color) {
    return new FontSpec(FontHelper.DEFAULT_FONT_PATH, size, color, 0, Color.BLACK);
  }

  /**
   * Factory method to create a FontSpec with size, color, and border settings using the default
   * font path.
   *
   * @param size the size of the font
   * @param color the font color
   * @param borderWidth the border width in pixels
   * @param borderColor the color of the border
   * @return a new FontSpec instance with the default path and specified styling
   */
  public static FontSpec of(int size, Color color, float borderWidth, Color borderColor) {
    return new FontSpec(FontHelper.DEFAULT_FONT_PATH, size, color, borderWidth, borderColor);
  }
}
