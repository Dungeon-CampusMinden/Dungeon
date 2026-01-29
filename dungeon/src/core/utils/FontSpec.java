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

  public FontSpec withSize(int size) {
    return new FontSpec(this.path, size, this.color, this.borderWidth, this.borderColor);
  }

  public FontSpec withColor(Color color) {
    return new FontSpec(this.path, this.size, color, this.borderWidth, this.borderColor);
  }

  public FontSpec withBorder(float borderWidth, Color borderColor) {
    return new FontSpec(this.path, this.size, this.color, borderWidth, borderColor);
  }

  public static FontSpec of(
      String path, int size, Color color, float borderWidth, Color borderColor) {
    return new FontSpec(path, size, color, borderWidth, borderColor);
  }

  public static FontSpec of(String path, int size, Color color) {
    return new FontSpec(path, size, color, 0, Color.BLACK);
  }

  public static FontSpec of(String path, int size) {
    return new FontSpec(path, size, Color.WHITE, 0, Color.BLACK);
  }

  public static FontSpec of(int size) {
    return new FontSpec(FontHelper.DEFAULT_FONT_PATH, size, Color.WHITE, 0, Color.BLACK);
  }

  public static FontSpec of(int size, Color color) {
    return new FontSpec(FontHelper.DEFAULT_FONT_PATH, size, color, 0, Color.BLACK);
  }

  public static FontSpec of(int size, Color color, float borderWidth, Color borderColor) {
    return new FontSpec(FontHelper.DEFAULT_FONT_PATH, size, color, borderWidth, borderColor);
  }
}
