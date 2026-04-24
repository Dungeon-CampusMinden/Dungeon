package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;

/**
 * World-space line draw command.
 *
 * @param from line start position in world coordinates
 * @param to line end position in world coordinates
 * @param color line color
 */
public record WorldLine(Point from, Point to, Color color) {}
