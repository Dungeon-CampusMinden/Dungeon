package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;

/**
 * Filled world-space circle draw command.
 *
 * @param center circle center in world coordinates
 * @param radius circle radius in world units
 * @param color fill color
 */
public record WorldCircleFill(Point center, float radius, Color color) {}
