package contrib.debug.draw;

import java.awt.Color;

/**
 * Filled world-space rectangle draw command.
 *
 * @param x world x-coordinate of the bottom-left corner
 * @param y world y-coordinate of the bottom-left corner
 * @param width rectangle width in world units
 * @param height rectangle height in world units
 * @param color fill color
 */
public record WorldFill(float x, float y, float width, float height, Color color) {}
