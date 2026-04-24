package contrib.debug.draw;

import java.awt.Color;

/**
 * World-space rectangle outline draw command.
 *
 * @param x world x-coordinate of the bottom-left corner
 * @param y world y-coordinate of the bottom-left corner
 * @param width rectangle width in world units
 * @param height rectangle height in world units
 * @param color outline color
 */
public record WorldRectangle(float x, float y, float width, float height, Color color) {}
