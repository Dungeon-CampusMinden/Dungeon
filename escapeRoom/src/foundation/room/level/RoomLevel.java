package foundation.room.level;

import contrib.entities.deco.Deco;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import foundation.room.model.RiddlePlacement;
import foundation.room.model.RoomLayout;
import foundation.room.model.RoomPoint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Generic Dungeon level materialized from one Foundation room layout. */
public final class RoomLevel extends DungeonLevel {
  /** Stable named-point key for the common Foundation door. */
  public static final String DOOR_POINT_NAME = "foundation_door";

  /** Stable named-point key for the common Foundation exit. */
  public static final String EXIT_POINT_NAME = "foundation_exit";

  /** Stable named-point key for the shared player start. */
  public static final String START_POINT_NAME = "foundation_start";

  /**
   * Parser-compatible constructor including decorations.
   *
   * @param layout engine-oriented level elements
   * @param designLabel tile design
   * @param namedPoints engine-oriented named points
   * @param decorations static decorations
   */
  public RoomLevel(
      final LevelElement[][] layout,
      final DesignLabel designLabel,
      final Map<String, Point> namedPoints,
      final List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations);
  }

  /**
   * Materializes a generic default-design level directly from neutral top-to-bottom room data.
   *
   * @param roomLayout validated neutral room layout
   * @return level with deterministic named points and one shared start
   */
  public static RoomLevel fromLayout(final RoomLayout roomLayout) {
    Objects.requireNonNull(roomLayout, "roomLayout");
    RoomLevel level =
        new RoomLevel(
            levelElements(roomLayout), DesignLabel.DEFAULT, namedPoints(roomLayout), List.of());
    level.startTiles().add(level.requiredTile(enginePoint(roomLayout, roomLayout.startPoint())));
    return level;
  }

  private Tile requiredTile(final Point point) {
    return tileAt(point)
        .orElseThrow(() -> new IllegalArgumentException("room start has no engine tile: " + point));
  }

  private static LevelElement[][] levelElements(final RoomLayout roomLayout) {
    List<String> rows = roomLayout.rows();
    int height = rows.size();
    int width = rows.getFirst().length();
    LevelElement[][] elements = new LevelElement[height][width];
    for (int topY = 0; topY < height; topY++) {
      int engineY = height - 1 - topY;
      for (int x = 0; x < width; x++) {
        elements[engineY][x] = levelElement(rows.get(topY).charAt(x));
      }
    }
    return elements;
  }

  private static LevelElement levelElement(final char marker) {
    return switch (marker) {
      case '#' -> LevelElement.WALL;
      case '.' -> LevelElement.FLOOR;
      case 'D' -> LevelElement.DOOR;
      case 'E' -> LevelElement.EXIT;
      default -> throw new IllegalArgumentException("unsupported Foundation room tile: " + marker);
    };
  }

  private static Map<String, Point> namedPoints(final RoomLayout roomLayout) {
    LinkedHashMap<String, Point> points = new LinkedHashMap<>();
    points.put(START_POINT_NAME, enginePoint(roomLayout, roomLayout.startPoint()));
    for (RiddlePlacement placement : roomLayout.riddlePlacements()) {
      placement
          .components()
          .forEach(
              component ->
                  points.put(
                      "component_" + component.componentId(),
                      enginePoint(roomLayout, component.point())));
      placement
          .hintPoint()
          .ifPresent(
              point -> points.put("hint_" + placement.riddleId(), enginePoint(roomLayout, point)));
    }
    points.put(DOOR_POINT_NAME, enginePoint(roomLayout, roomLayout.doorPoint()));
    points.put(EXIT_POINT_NAME, enginePoint(roomLayout, roomLayout.exitPoint()));
    return points;
  }

  private static Point enginePoint(final RoomLayout roomLayout, final RoomPoint point) {
    return new Point(point.x(), roomLayout.rows().size() - 1 - point.y());
  }
}
