package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;

/**
 * Screen-space rectangle draw command.
 *
 * @param topLeft top-left corner in screen pixels
 * @param width rectangle width in pixels
 * @param height rectangle height in pixels
 * @param fill fill color, or {@code null} for no fill
 * @param outline outline color, or {@code null} for no outline
 */
public record ScreenRectangle(Point topLeft, int width, int height, Color fill, Color outline) {}
