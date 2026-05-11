package contrib.hud.elements.richlabel;

import com.badlogic.gdx.graphics.Color;

/**
 * A single word of text with an associated color, optional size override, optional shake, and an
 * optional font path override.
 *
 * @param word the text content (single word, may include trailing space)
 * @param color the color to render this word in, or null to use the default font color
 * @param sizeOverride the font size override, or -1 to use the default
 * @param shake the shake effect parameters, or null for no shake
 * @param fontPathOverride the font file path to use instead of the default, or null for default
 */
public record TextRun(
    String word, Color color, int sizeOverride, ShakeEffect shake, String fontPathOverride)
    implements Run {

  /**
   * Backwards-compatible constructor that creates a TextRun without a font path override.
   *
   * @param word the text content
   * @param color the color or null
   * @param sizeOverride the size override or -1
   * @param shake the shake effect or null
   */
  public TextRun(String word, Color color, int sizeOverride, ShakeEffect shake) {
    this(word, color, sizeOverride, shake, null);
  }
}
