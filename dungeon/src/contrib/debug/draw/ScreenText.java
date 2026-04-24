package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;

/**
 * Screen-space text draw command.
 *
 * @param text text to render
 * @param screen screen position in pixels
 * @param color text color
 */
public record ScreenText(String text, Point screen, Color color) {}
