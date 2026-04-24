package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;

/**
 * Screen-space circular marker draw command.
 *
 * @param center center position in screen pixels
 * @param diameterPx marker diameter in pixels
 * @param fillColor fill color, or {@code null} for no fill
 * @param outlineColor outline color, or {@code null} for no outline
 */
public record ScreenMarker(Point center, int diameterPx, Color fillColor, Color outlineColor) {}
