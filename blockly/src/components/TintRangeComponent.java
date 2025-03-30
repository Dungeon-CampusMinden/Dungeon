package components;

import core.Component;
import core.Game;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import java.util.HashMap;
import java.util.Map;
import utils.Direction;

/** Stores information about a range on Tiles that needs to be tinted. */
public class TintRangeComponent implements Component {
  private static final int DEFAULT_COLOR = 0xFF0000FF; // Default color (red)

  private final Coordinate origin;
  private final int range;
  private final Direction direction;
  private final int color;

  // Map to store the original color of the Tiles
  private final Map<Coordinate, Integer> tintMap = new HashMap<>();

  /**
   * Creates a new TintRangeComponent.
   *
   * <p>This constructor sets the default color to red.
   *
   * @param origin The origin of the range.
   * @param range The range of the tint.
   * @param direction The direction of the tint.
   */
  public TintRangeComponent(Coordinate origin, int range, Direction direction) {
    this(origin, range, direction, DEFAULT_COLOR);
  }

  /**
   * Creates a new TintRangeComponent.
   *
   * @param origin The origin of the range.
   * @param range The range of the tint.
   * @param direction The direction of the tint.
   * @param color The color of the tint.
   */
  public TintRangeComponent(Coordinate origin, int range, Direction direction, int color) {
    this.origin = origin;
    this.range = range;
    this.direction = direction;
    this.color = color;
  }

  /** Applies the tint to the affected Tiles. */
  public void applyTint() {
    Tile[] tiles = affectedTiles();
    for (Tile tile : tiles) {
      if (tile != null) {
        tintTile(tile);
      }
    }
  }

  /** Removes the tint from the affected Tiles. */
  public void removeTint() {
    Tile[] tiles = affectedTiles();
    for (Tile tile : tiles) {
      if (tile != null) {
        removeTint(tile);
      }
    }
  }

  private Tile[] affectedTiles() {
    ILevel level = Game.currentLevel();
    if (level == null) return new Tile[0];
    Tile[] tiles = new Tile[range];
    Coordinate start = origin;
    int currentRange = 0;
    while (currentRange < range) {
      Coordinate end =
          start.add(new Coordinate(direction.x() * currentRange, direction.y() * currentRange));
      if (!utils.LevelUtils.canSee(start, end, direction)) {
        break;
      }
      tiles[currentRange] = level.tileAt(end);
      currentRange++;
    }
    return tiles;
  }

  private void tintTile(Tile tile) {
    if (tile == null) return;

    int originalColor = tile.tintColor();
    tintMap.put(tile.coordinate(), originalColor);
    tile.tintColor(color);
  }

  private void removeTint(Tile tile) {
    if (tile == null) return;

    Integer originalColor = tintMap.get(tile.coordinate());
    if (originalColor != null) {
      tile.tintColor(originalColor);
      tintMap.remove(tile.coordinate());
    }
  }
}
