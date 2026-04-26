package core.game.render.level;

/**
 * Represents metadata for the rendering of a visible level buffer in world space.
 *
 * <p>This record provides details about the spatial context of the rendered level, including the
 * minimum x-coordinate of visible tiles, the maximum y-coordinate of visible tiles, and the size of
 * a single tile in pixels.
 *
 * <p>The {@code LevelPassContext} is commonly used in level effects to translate buffer-space
 * coordinates (e.g., pixel locations) into corresponding world-space locations or to assist in
 * applying transformations during post-processing.
 *
 * @param minTileX the minimum x-coordinate of visible tiles in the level
 * @param maxTileY the maximum y-coordinate of visible tiles in the level
 * @param tilePx the size of a single tile in pixels
 */
public record LevelPassContext(int minTileX, int maxTileY, int tilePx) {}
