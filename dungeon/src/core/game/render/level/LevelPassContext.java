package core.game.render.level;

/**
 * Represents contextual metadata related to the rendering or processing of a level's visible tiled region.
 *
 * <ul>
 *   <li>{@code minTileX}: The minimum X-coordinate of the visible tile space.</li>
 *   <li>{@code maxTileY}: The maximum Y-coordinate of the visible tile space.</li>
 *   <li>{@code tilePx}: The pixel size of a single tile, used for scaling or transformations.</li>
 * </ul>
 *
 * <p>Instances of this class are immutable and are frequently used to pass relevant data
 * when applying post-processing effects to the level layer or for other rendering-related purposes.
 */
public record LevelPassContext(int minTileX, int maxTileY, int tilePx) {}
