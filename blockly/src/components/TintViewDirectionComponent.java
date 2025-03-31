package components;

import core.Component;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import utils.Direction;
import utils.LevelUtils;

/** This component is used to tint tiles of a view direction of an entity. */
public class TintViewDirectionComponent implements Component {
  private static final int DEFAULT_COLOR = 0xFF0000FF; // Default color (red)

  private final Coordinate origin;
  private final int range;
  private final PositionComponent positionComponent;
  private final int color;

  // Map to store the original color of the tinted Tiles
  private final Map<Coordinate, Integer> tintMap = new HashMap<>();

  /**
   * Creates a new TintRangeComponent with default color (red).
   *
   * @param origin The origin of the range.
   * @param range The range of the tint.
   * @param positionComponent The position component of the entity.
   */
  public TintViewDirectionComponent(
      Coordinate origin, int range, PositionComponent positionComponent) {
    this(origin, range, positionComponent, DEFAULT_COLOR);
  }

  /**
   * Creates a new TintRangeComponent.
   *
   * @param origin The origin of the range.
   * @param range The range of the tint.
   * @param positionComponent The position component of the entity.
   * @param color The color of the tint.
   */
  public TintViewDirectionComponent(
      Coordinate origin, int range, PositionComponent positionComponent, int color) {
    this.origin = origin;
    this.range = range;
    this.positionComponent = positionComponent;
    this.color = color;
  }

  /**
   * Applies the tint to the affected Tiles. Removes tint from tiles that are no longer affected.
   */
  public void applyTint() {
    Set<Coordinate> currentlyAffectedCoordinates = new HashSet<>();
    ILevel level = Game.currentLevel();
    if (level == null) return;

    // Get currently affected tiles
    for (Tile tile : affectedTiles()) {
      Coordinate coord = tile.coordinate();
      currentlyAffectedCoordinates.add(coord);

      // Apply tint if not already applied
      if (!tintMap.containsKey(coord)) {
        tintMap.put(coord, tile.tintColor());
        tile.tintColor(color);
      }
    }

    // Remove tint from tiles no longer affected
    tintMap
        .keySet()
        .removeIf(
            coord -> {
              if (!currentlyAffectedCoordinates.contains(coord)) {
                Tile tile = level.tileAt(coord);
                tintTile(tile, tintMap.get(coord));
                return true; // Remove from tintMap
              }
              return false; // Keep in tintMap
            });
  }

  /** Removes the tint from all affected Tiles. */
  public void removeTint() {
    ILevel level = Game.currentLevel();
    if (level == null) return;

    for (Map.Entry<Coordinate, Integer> entry : tintMap.entrySet()) {
      Tile tile = level.tileAt(entry.getKey());
      tintTile(tile, entry.getValue());
    }
    tintMap.clear();
  }

  /**
   * Gets all tiles affected by this tint component.
   *
   * @return List of affected tiles
   */
  private ArrayList<Tile> affectedTiles() {
    ILevel level = Game.currentLevel();
    ArrayList<Tile> tiles = new ArrayList<>(range);
    if (level == null) return tiles;

    for (int i = 0; i < range; i++) {
      Coordinate targetCoord = origin.add(new Coordinate(direction().x() * i, direction().y() * i));

      // Stop if we can't see further in this direction
      if (!LevelUtils.canSee(origin, targetCoord, direction())) {
        break;
      }

      Tile tile = level.tileAt(targetCoord);
      if (tile != null) {
        tiles.add(tile);
      }
    }
    return tiles;
  }

  private Direction direction() {
    return Direction.fromPositionCompDirection(positionComponent.viewDirection());
  }

  private void tintTile(Tile tile, int color) {
    if (tile == null) return;
    tile.tintColor(color);
  }
}
