package core.level.utils;

import core.level.Tile;
import core.level.elements.ILevel;
import core.utils.Point;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Utility methods for layout transforms that must keep level metadata in sync. */
public final class LevelTransformations {

  private LevelTransformations() {}

  /**
   * Creates a resized copy of a level layout.
   *
   * <p>Existing cells keep their current {@link LevelElement}; new cells are filled with {@link
   * LevelElement#SKIP}. Cells outside the new bounds are omitted.
   *
   * @param layout the current tile layout
   * @param rows the target row count
   * @param cols the target column count
   * @return a resized level-element layout
   */
  public static LevelElement[][] resizedLayout(Tile[][] layout, int rows, int cols) {
    LevelElement[][] newLayout = new LevelElement[rows][cols];

    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        if (y >= layout.length || x >= layout[0].length) {
          newLayout[y][x] = LevelElement.SKIP;
        } else {
          newLayout[y][x] = layout[y][x].levelElement();
        }
      }
    }

    return newLayout;
  }

  /**
   * Creates a shifted copy of a level layout.
   *
   * <p>Cells shifted outside the layout are omitted. Newly exposed cells are filled with {@link
   * LevelElement#SKIP}.
   *
   * @param layout the current tile layout
   * @param xOffset horizontal shift offset
   * @param yOffset vertical shift offset
   * @return a shifted level-element layout
   */
  public static LevelElement[][] shiftedLayout(Tile[][] layout, int xOffset, int yOffset) {
    int rows = layout.length;
    int cols = layout[0].length;
    LevelElement[][] newLayout = new LevelElement[rows][cols];

    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        int oldY = y - yOffset;
        int oldX = x - xOffset;

        if (oldY >= 0 && oldY < rows && oldX >= 0 && oldX < cols) {
          newLayout[y][x] = layout[oldY][oldX].levelElement();
        } else {
          newLayout[y][x] = LevelElement.SKIP;
        }
      }
    }

    return newLayout;
  }

  /**
   * Captures the current start-tile coordinates.
   *
   * @param level the level whose start tiles should be captured
   * @return start-tile coordinates in list order
   */
  public static List<Coordinate> startTileCoordinates(ILevel level) {
    return level.startTiles().stream().filter(Objects::nonNull).map(Tile::coordinate).toList();
  }

  /**
   * Remaps start tiles to the level's current tile objects at the given coordinates.
   *
   * <p>Coordinates outside the current layout are dropped.
   *
   * @param level the level whose start tiles should be remapped
   * @param coordinates the coordinates to map to current tile objects
   */
  public static void remapStartTiles(ILevel level, List<Coordinate> coordinates) {
    level.startTiles().clear();
    coordinates.stream()
        .map(level::tileAt)
        .flatMap(Optional::stream)
        .forEach(level.startTiles()::add);
  }

  /**
   * Remaps existing start tiles to current tile objects at their current coordinates.
   *
   * @param level the level whose start tiles should be remapped
   */
  public static void remapStartTiles(ILevel level) {
    remapStartTiles(level, startTileCoordinates(level));
  }

  /**
   * Translates start-tile positions and remaps them to current tile objects.
   *
   * <p>Translated positions outside the current layout are dropped.
   *
   * @param level the level whose start tiles should be translated
   * @param xOffset horizontal shift offset
   * @param yOffset vertical shift offset
   */
  public static void translateStartTiles(ILevel level, int xOffset, int yOffset) {
    List<Coordinate> shiftedCoordinates =
        startTileCoordinates(level).stream()
            .map(coordinate -> new Coordinate(coordinate.x() + xOffset, coordinate.y() + yOffset))
            .toList();

    remapStartTiles(level, shiftedCoordinates);
  }

  /**
   * Translates all named points of a level.
   *
   * @param level the level whose named points should be translated
   * @param xOffset horizontal shift offset
   * @param yOffset vertical shift offset
   */
  public static void translateNamedPoints(ILevel level, int xOffset, int yOffset) {
    level
        .namedPoints()
        .replaceAll((_, point) -> new Point(point.x() + xOffset, point.y() + yOffset));
  }
}
