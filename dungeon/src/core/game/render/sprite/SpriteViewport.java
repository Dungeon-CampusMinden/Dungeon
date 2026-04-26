package core.game.render.sprite;

/**
 * A record representing the viewport parameters for sprite rendering.
 *
 * @param offsetX screen offset for X coordinate in pixels
 * @param offsetY screen offset for Y coordinate in pixels
 * @param minTileX minimum visible tile X coordinate
 * @param maxTileX maximum visible tile X coordinate
 * @param minTileY minimum visible tile Y coordinate
 * @param maxTileY maximum visible tile Y coordinate
 * @param levelHeight height of the current level in tiles
 * @param tilePx size of a tile in pixels
 */
record SpriteViewport(
    double offsetX,
    double offsetY,
    int minTileX,
    int maxTileX,
    int minTileY,
    int maxTileY,
    int levelHeight,
    int tilePx) {}
