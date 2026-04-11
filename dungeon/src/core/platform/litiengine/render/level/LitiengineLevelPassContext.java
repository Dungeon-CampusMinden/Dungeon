package core.platform.litiengine.render.level;

/**
 * World-space metadata for one rendered LITIENGINE level-pass buffer.
 *
 * @param minTileX world x coordinate of the left-most visible tile in the buffer
 * @param maxTileY world y coordinate of the top-most visible tile in the buffer
 * @param tilePx number of pixels per world tile inside the buffer
 */
public record LitiengineLevelPassContext(int minTileX, int maxTileY, int tilePx) {}
