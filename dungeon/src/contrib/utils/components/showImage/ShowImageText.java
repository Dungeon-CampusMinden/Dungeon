package contrib.utils.components.showImage;

/**
 * Engine-neutral text configuration for show-image dialogs.
 *
 * <p>The text color is stored as RGBA8888 so the payload can be used by different rendering
 * backends without depending on engine-specific color classes.
 *
 * @param text the text to display
 * @param scale the scale of the text
 * @param colorRgba8888 the text color encoded as RGBA8888
 */
public record ShowImageText(String text, float scale, int colorRgba8888) {

  /** Default text color in RGBA8888: opaque black. */
  public static final int DEFAULT_COLOR_RGBA8888 = 0x000000ff;

  /**
   * Creates a text config with the specified text and scale, using opaque black.
   *
   * @param text the text to display
   * @param scale the scale of the text
   */
  public ShowImageText(String text, float scale) {
    this(text, scale, DEFAULT_COLOR_RGBA8888);
  }

  /**
   * Creates a text config with the specified text, using scale {@code 1f} and opaque black.
   *
   * @param text the text to display
   */
  public ShowImageText(String text) {
    this(text, 1f, DEFAULT_COLOR_RGBA8888);
  }

  /**
   * Returns the configured color as RGBA8888.
   *
   * <p>This method intentionally keeps the old method name so existing call sites such as
   * {@code ShowImageSystem} and {@code DialogUtils} do not need to change in this commit.
   *
   * @return the configured color as RGBA8888
   */
  public int rgba8888Color() {
    return colorRgba8888;
  }

  /**
   * Creates a text config with an opaque RGB color.
   *
   * @param text the text to display
   * @param scale the scale of the text
   * @param red red channel in range 0..255
   * @param green green channel in range 0..255
   * @param blue blue channel in range 0..255
   * @return a new text config
   */
  public static ShowImageText ofRgb(String text, float scale, int red, int green, int blue) {
    return new ShowImageText(text, scale, rgba8888(red, green, blue, 255));
  }

  /**
   * Creates a text config with an RGBA color.
   *
   * @param text the text to display
   * @param scale the scale of the text
   * @param red red channel in range 0..255
   * @param green green channel in range 0..255
   * @param blue blue channel in range 0..255
   * @param alpha alpha channel in range 0..255
   * @return a new text config
   */
  public static ShowImageText ofRgba(
    String text, float scale, int red, int green, int blue, int alpha) {
    return new ShowImageText(text, scale, rgba8888(red, green, blue, alpha));
  }

  private static int rgba8888(int red, int green, int blue, int alpha) {
    return (clamp(red) << 24) | (clamp(green) << 16) | (clamp(blue) << 8) | clamp(alpha);
  }

  private static int clamp(int value) {
    return Math.clamp(value, 0, 255);
  }
}
