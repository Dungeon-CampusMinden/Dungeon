package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;

/**
 * World-space circle outline draw command.
 *
 * @param center circle center in world coordinates
 * @param radius circle radius in world units
 * @param color outline color
 */
public record WorldCircleOutline(Point center, float radius, Color color) {}
