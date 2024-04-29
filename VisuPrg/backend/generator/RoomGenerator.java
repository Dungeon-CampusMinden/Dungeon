package generator;

import static core.level.elements.ILevel.RANDOM;

import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

// Todo - Cleanup
public class RoomGenerator {
  private static final int WALL_BUFFER = 2;

  public ILevel level(final DesignLabel designLabel) {
    return new TileLevel(layout(LevelSize.LARGE), designLabel);
  }

  private LevelElement[][] layout(final LevelSize size) {
    return generateRoom(size, RANDOM.nextLong());
  }

  private LevelElement[][] generateRoom(final LevelSize size, long seed) {

    Area maxArea;
    switch (size) {
      case LARGE -> maxArea = new Area(15, 15);

      default -> maxArea = new Area(25, 25);
    }

    // Initialize layout with additional buffer for wall and skip layer
    LevelElement[][] layout =
        new LevelElement[maxArea.y + WALL_BUFFER * 2][maxArea.x + WALL_BUFFER * 2];

    // Fill with skip
    for (int y = 0; y < layout.length; y++) {
      for (int x = 0; x < layout[0].length; x++) {
        layout[y][x] = LevelElement.SKIP;
      }
    }

    for (int y = WALL_BUFFER; y < layout.length - WALL_BUFFER; y++) {
      for (int x = WALL_BUFFER; x < layout[0].length - WALL_BUFFER; x++) {
        layout[y][x] = LevelElement.FLOOR;
      }
    }

    // place Walls
    for (int y = 1; y < layout.length - 1; y++) {
      for (int x = 1; x < layout[0].length - 1; x++) {
        if (layout[y][x] == LevelElement.SKIP && neighborsFloor(layout, y, x))
          layout[y][x] = LevelElement.WALL;
      }
    }

    return layout;
  }

  /**
   * Checks if a Tile at given coordinate in the layout neighbors a FloorTile.
   *
   * @param layout The layout of the room.
   * @param y Y-coordinate of Tile to check.
   * @param x X-coordinate of Tile to check.
   * @return true if at least one FloorTile is neighboring the Tile.
   */
  private boolean neighborsFloor(final LevelElement[][] layout, int y, int x) {
    int floorNeighbors = 0;
    if (layout[y + 1][x - 1] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y + 1][x] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y + 1][x + 1] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y][x - 1] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y][x + 1] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y - 1][x - 1] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y - 1][x] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    if (layout[y - 1][x + 1] == LevelElement.FLOOR) {
      floorNeighbors++;
    }
    return floorNeighbors > 0;
  }

  private record MinMaxValue(int min, int max) {}

  private record Area(int x, int y) {}
}
