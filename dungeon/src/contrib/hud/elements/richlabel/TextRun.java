package contrib.hud.elements.richlabel;

import com.badlogic.gdx.graphics.Color;

/**
 * A single word of text with an associated color, optional size override, and optional shake.
 *
 * @param word the text content (single word, may include trailing space)
 * @param color the color to render this word in, or null to use the default font color
 * @param sizeOverride the font size override, or -1 to use the default
 * @param shake the shake effect parameters, or null for no shake
 */
public record TextRun(String word, Color color, int sizeOverride, ShakeEffect shake)
    implements Run {}
