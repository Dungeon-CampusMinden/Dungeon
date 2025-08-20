package components;

import contrib.utils.LevelUtils;
import core.Component;
import core.Game;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.utils.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** This component is used to tint tiles of a view direction of an entity. */
public class TintDirectionComponent implements Component {
  private static final int DEFAULT_COLOR = 0xFF0000FF; // Default color (red)

  private final Coordinate origin;
  private final int range;
  private final int color;

  // Map to store the original color of the tinted Tiles
  private final Map<Coordinate, Integer> tintMap = new HashMap<>();

  /**
   * Creates a new TintRangeComponent with default color (red).
   *
   * @param origin The origin of the range.
   * @param range The range of the tint.
   */
  public TintDirectionComponent(Coordinate origin, int range) {
    this(origin, range, DEFAULT_COLOR);
  }

  /**
   * Creates a new TintRangeComponent.
   *
   * @param origin The origin of the range.
   * @param range The range of the tint.
   * @param color The color of the tint.
   */
  public TintDirectionComponent(Coordinate origin, int range, int color) {
    this.origin = origin;
    this.range = range;
    this.color = color;
  }

  /**
   * Gets all the original colors of the tiles in the tint range.
   *
   * @return A set of coordinates with their original colors.
   */
  public Set<Coordinate> originalColors() {
    Set<Coordinate> originalColors = new HashSet<>();
    for (Map.Entry<Coordinate, Integer> entry : tintMap.entrySet()) {
      originalColors.add(entry.getKey());
    }
    return originalColors;
  }

  /**
   * Gets the original color of the tile at the given coordinate.
   *
   * @param coordinate The coordinate of the tile.
   * @return The original color of the tile, or -1 if not found.
   */
  public int originalColor(Coordinate coordinate) {
    return tintMap.getOrDefault(coordinate, -1);
  }

  /**
   * Adds the original color of the tile at the given coordinate.
   *
   * <p>If the coordinate already exists in the map, it will not be added again to keep the original
   * color.
   *
   * @param coordinate The coordinate of the tile.
   * @param color The original color of the tile.
   */
  public void originalColor(Coordinate coordinate, int color) {
    if (tintMap.containsKey(coordinate)) return;
    tintMap.put(coordinate, color);
  }

  /**
   * Removes the original color of the tile at the given coordinate.
   *
   * @param coordinate The coordinate of the tile.
   */
  public void removeOriginalColor(Coordinate coordinate) {
    tintMap.remove(coordinate);
  }

  /** Clears all original colors. */
  public void clearOriginalColors() {
    tintMap.clear();
  }

  /**
   * Gets the color of the tint.
   *
   * @return The color of the tint.
   */
  public int color() {
    return color;
  }

  /**
   * Gets all tiles affected by this tint component.
   *
   * @param direction The viewing direction of the entity that is using this component.
   * @return List of affected tiles
   */
  public ArrayList<Tile> affectedTiles(Direction direction) {
    ILevel level = Game.currentLevel().orElse(null);
    ArrayList<Tile> tiles = new ArrayList<>(range);
    if (level == null) return tiles;

    for (int i = 0; i < range; i++) {
      Coordinate targetCoord = origin.translate(direction.scale(i));

      // Stop if we can't see further in this direction
      if (!LevelUtils.canSee(origin, targetCoord, direction)) {
        break;
      }
      level.tileAt(targetCoord).ifPresent(tiles::add);
    }
    return tiles;
  }
}
