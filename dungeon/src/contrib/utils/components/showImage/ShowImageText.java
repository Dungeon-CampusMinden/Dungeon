package contrib.utils.components.showImage;

import com.badlogic.gdx.graphics.Color;

/**
 * A record that holds configuration for displaying text on an image.
 *
 * @param text the text to display
 * @param scale the scale of the text
 * @param color the color of the text
 */
public record ShowImageText(String text, float scale, Color color) {

  /**
   * Creates a text config with the specified text and scale, in black.
   *
   * @param text the text to display
   * @param scale the scale of the text
   */
  public ShowImageText(String text, float scale) {
    this(text, scale, Color.BLACK);
  }

  /**
   * Creates a text config with the specified text, in black and with a scale of 1.
   *
   * @param text the text to display
   */
  public ShowImageText(String text) {
    this(text, 1f, Color.BLACK);
  }

  /**
   * Encodes the configured libGDX color as RGBA8888 so it can be transported through the dialog
   * context without forcing the target renderer to depend on libGDX color APIs.
   *
   * @return the configured color as RGBA8888
   */
  public int rgba8888Color() {
    return Color.rgba8888(color == null ? Color.BLACK : color);
  }
}
