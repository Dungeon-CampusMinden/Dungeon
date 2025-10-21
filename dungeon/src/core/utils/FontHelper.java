package core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for loading, caching, and reusing bitmap fonts. Fonts are generated from TrueType
 * font files with optional color and border customization.
 */
public class FontHelper {

  /** Internal path to default font file. */
  public static final String DEFAULT_FONT_PATH = "fonts/Roboto-SemiBold.ttf";

  private static final Map<FontEntry, BitmapFont> fontStorage = new HashMap<>();

  /**
   * Loads or retrieves the default font: Roboto-SemiBold at size 16.
   *
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getDefaultFont() {
    return getDefaultFont(16);
  }

  /**
   * Loads or retrieves the default font: Roboto-SemiBold.
   *
   * @param size the size of the font
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getDefaultFont(int size) {
    return getFont(DEFAULT_FONT_PATH, size);
  }

  /**
   * Loads or retrieves a cached font with default settings. Default values: size = 16, color =
   * white, border width = 1, border color = black.
   *
   * @param path the internal path to the TrueType font file
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getFont(String path) {
    return getFont(path, 16, Color.WHITE, 1, Color.BLACK);
  }

  /**
   * Loads or retrieves a cached font with a specified size. Default values: color = white, border
   * width = 1, border color = black.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getFont(String path, int size) {
    return getFont(path, size, Color.WHITE, 1, Color.BLACK);
  }

  /**
   * Loads or retrieves a cached font with a specified size and color. Default values: border width
   * = 1, border color = black.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @param color the font color
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getFont(String path, int size, Color color) {
    return getFont(path, size, color, 1, Color.BLACK);
  }

  /**
   * Loads or retrieves a cached font with a specified size, color, and border width. Default value:
   * border color = black.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @param color the font color
   * @param borderWidth the border width in pixels
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getFont(String path, int size, Color color, float borderWidth) {
    return getFont(path, size, color, borderWidth, Color.BLACK);
  }

  /**
   * Loads or retrieves a cached font with full customization options. If the font with the same
   * parameters was previously generated, the cached version is returned.
   *
   * @param path the internal path to the TrueType font file
   * @param size the size of the font
   * @param color the font color
   * @param borderWidth the border width in pixels
   * @param borderColor the color of the border
   * @return the generated or cached {@link BitmapFont}
   */
  public static BitmapFont getFont(
      String path, int size, Color color, float borderWidth, Color borderColor) {

    FontEntry entry = new FontEntry(path, size, color, borderWidth, borderColor);

    if (!fontStorage.containsKey(entry)) {
      FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
      FreeTypeFontGenerator.FreeTypeFontParameter params =
          new FreeTypeFontGenerator.FreeTypeFontParameter();

      params.size = size;
      params.color = color;
      params.borderWidth = borderWidth;
      params.borderColor = borderColor;
      params.genMipMaps = true;

      BitmapFont font = generator.generateFont(params);
      fontStorage.put(entry, font);
      generator.dispose();
    }

    return fontStorage.get(entry);
  }

  /**
   * Internal record used to cache fonts based on their unique configuration.
   *
   * @param path the font file path
   * @param size the font size
   * @param color the font color
   * @param borderWidth the border width
   * @param borderColor the border color
   */
  private record FontEntry(
      String path, int size, Color color, float borderWidth, Color borderColor) {}
}
