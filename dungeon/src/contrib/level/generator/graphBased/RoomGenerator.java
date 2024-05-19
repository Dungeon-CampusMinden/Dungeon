package contrib.level.generator.graphBased;

import static core.level.elements.ILevel.RANDOM;

import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import java.util.ArrayList;
import java.util.Random;

/**
 * Generator for a random room of a given size and adds doors at given directions.
 *
 * <p>The generation of the layout can be initialized with a seed to generate the same room again. A
 * room consists of a baseFloor area and random extension areas to the corners or sides of the
 * baseFloor. Walls surrounding the areas are generated. Holes are generated at random FloorTiles.
 * Supports are generated in rooms with a big baseFloor. Finally, doors are generated at the
 * outermost walls for every given door direction.
 */
public final class RoomGenerator {

  private static final int WALL_BUFFER = 2;
  private static final float EXTEND_TO_SIDES = 0.5f;
  private static final float PROBABILITY_SIDE = 0.75f;
  private static final float PROBABILITY_CORNER = 0.75f;
  private static final float PROBABILITY_HOLE = 0.02f;
  private static final int SMALL_MIN_X_SIZE = 5;
  private static final int SMALL_MIN_Y_SIZE = 5;
  private static final int SMALL_MAX_X_SIZE = 8;
  private static final int SMALL_MAX_Y_SIZE = 8;
  private static final int MEDIUM_MIN_X_SIZE = 12;
  private static final int MEDIUM_MIN_Y_SIZE = 12;
  private static final int MEDIUM_MAX_X_SIZE = 16;
  private static final int MEDIUM_MAX_Y_SIZE = 16;
  private static final int BIG_MIN_X_SIZE = 20;
  private static final int BIG_MIN_Y_SIZE = 20;
  private static final int BIG_MAX_X_SIZE = 24;
  private static final int BIG_MAX_Y_SIZE = 24;
  private Random random;

  /**
   * Get the layout of the room for debugging as String.
   *
   * @param layout The layout of the room.
   * @param size The size of the room.
   * @return WTF? .
   */
  public static String layoutToString(final LevelElement[][] layout, final LevelSize size) {
    StringBuilder result = new StringBuilder();
    result.append("LevelSize: ").append(size.name()).append("\n");
    result.append("xSize: ").append(layout[0].length).append("\n");
    result.append("ySize: ").append(layout.length).append("\n");

    for (int y = layout.length - 1; y >= 0; y--) {
      for (int x = 0; x < layout[0].length; x++) {
        switch (layout[y][x]) {
          case SKIP -> result.append("  ");
          case FLOOR -> result.append(". ");
          case WALL -> result.append("W ");
          case EXIT -> result.append("E ");
          case HOLE -> result.append("H ");
          case DOOR -> result.append("D ");
        }
      }
      result.append("\n");
    }
    return result.toString();
  }

  /**
   * Generates a random room layout with the given parameters.
   *
   * @param size Size of the room.
   * @param doors Array of DoorDirections to specify where doors should be generated.
   * @return The generated room layout.
   */
  public LevelElement[][] layout(final LevelSize size, final LevelNode[] doors) {
    return generateRoom(size, RANDOM.nextLong(), doors);
  }

  /**
   * Generates a room layout with the given parameters.
   *
   * @param size Size of the room.
   * @param seed Seed to initialize the random number generator.
   * @param doors Array of DoorDirections to specify where doors should be generated.
   * @return The generated room layout.
   */
  private LevelElement[][] generateRoom(final LevelSize size, long seed, final LevelNode[] doors) {
    // Initialize random number generator with seed
    random = new Random(seed);

    // Define max room size
    Area maxArea;
    switch (size) {
      case SMALL ->
          maxArea =
              new Area(
                  random.nextInt(SMALL_MIN_X_SIZE, SMALL_MAX_X_SIZE + 1),
                  random.nextInt(SMALL_MIN_Y_SIZE, SMALL_MAX_Y_SIZE + 1));
      case LARGE ->
          maxArea =
              new Area(
                  random.nextInt(BIG_MIN_X_SIZE, BIG_MAX_X_SIZE + 1),
                  random.nextInt(BIG_MIN_Y_SIZE, BIG_MAX_Y_SIZE + 1));
      default ->
          maxArea =
              new Area(
                  random.nextInt(MEDIUM_MIN_X_SIZE, MEDIUM_MAX_X_SIZE + 1),
                  random.nextInt(MEDIUM_MIN_Y_SIZE, MEDIUM_MAX_Y_SIZE + 1));
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

    // Add BaseFloor in the center
    final MinMaxValue BASE_FLOOR_X_SIZE;
    final MinMaxValue BASE_FLOOR_Y_SIZE;
    switch (size) {
      case SMALL -> {
        BASE_FLOOR_X_SIZE = new MinMaxValue(SMALL_MIN_X_SIZE, maxArea.x);
        BASE_FLOOR_Y_SIZE = new MinMaxValue(SMALL_MIN_Y_SIZE, maxArea.y);
      }
      case LARGE -> {
        BASE_FLOOR_X_SIZE = new MinMaxValue(BIG_MIN_X_SIZE - 10, maxArea.x - 4);
        BASE_FLOOR_Y_SIZE = new MinMaxValue(BIG_MIN_Y_SIZE - 10, maxArea.y - 4);
      }
      default -> {
        BASE_FLOOR_X_SIZE = new MinMaxValue(MEDIUM_MIN_X_SIZE - 8, maxArea.x - 3);
        BASE_FLOOR_Y_SIZE = new MinMaxValue(MEDIUM_MIN_Y_SIZE - 8, maxArea.y - 3);
      }
    }
    final MinMaxValue BASE_FLOOR_PADDING_X =
        new MinMaxValue(
            (maxArea.x - BASE_FLOOR_X_SIZE.max + 1) / 2, (maxArea.x - BASE_FLOOR_X_SIZE.min) / 2);
    final MinMaxValue BASE_FLOOR_PADDING_Y =
        new MinMaxValue(
            (maxArea.y - BASE_FLOOR_Y_SIZE.max + 1) / 2, (maxArea.y - BASE_FLOOR_Y_SIZE.min) / 2);

    Area baseFloorPadding =
        new Area(
            BASE_FLOOR_PADDING_X.min == BASE_FLOOR_PADDING_X.max
                ? BASE_FLOOR_PADDING_X.min
                : random.nextInt(BASE_FLOOR_PADDING_X.min, BASE_FLOOR_PADDING_X.max + 1),
            BASE_FLOOR_PADDING_Y.min == BASE_FLOOR_PADDING_Y.max
                ? BASE_FLOOR_PADDING_Y.min
                : random.nextInt(BASE_FLOOR_PADDING_Y.min, BASE_FLOOR_PADDING_Y.max + 1));
    Area baseFloor =
        new Area(maxArea.x - 2 * baseFloorPadding.x, maxArea.y - 2 * baseFloorPadding.y);

    for (int y = WALL_BUFFER + baseFloorPadding.y;
        y < layout.length - WALL_BUFFER - baseFloorPadding.y;
        y++) {
      for (int x = WALL_BUFFER + baseFloorPadding.x;
          x < layout[0].length - WALL_BUFFER - baseFloorPadding.x;
          x++) {
        layout[y][x] = LevelElement.FLOOR;
      }
    }

    // Extend base floor
    // Small rooms cannot extend to the corners
    // Medium and Big Rooms can extend to corner or sides
    if (size == LevelSize.SMALL || random.nextFloat() < EXTEND_TO_SIDES) {
      extendToSides(layout, maxArea, baseFloorPadding, baseFloor);
    } else {
      extendToCorners(layout, maxArea, baseFloorPadding, baseFloor);
    }

    // place Walls
    for (int y = 1; y < layout.length - 1; y++) {
      for (int x = 1; x < layout[0].length - 1; x++) {
        if (layout[y][x] == LevelElement.SKIP && neighborsFloor(layout, y, x))
          layout[y][x] = LevelElement.WALL;
      }
    }

    // add random Holes
    for (int y = WALL_BUFFER; y < layout.length - WALL_BUFFER; y++) {
      for (int x = WALL_BUFFER; x < layout[0].length - WALL_BUFFER; x++) {
        if (layout[y][x] == LevelElement.FLOOR && random.nextFloat() < PROBABILITY_HOLE)
          layout[y][x] = LevelElement.HOLE;
      }
    }

    // add supports for large rooms
    if (baseFloor.x >= 15 && baseFloor.y >= 15) {
      addSupports(layout, baseFloorPadding, baseFloor);
    }

    addDoors(doors, maxArea, layout, size);
    return layout;
  }

  /**
   * Extends the baseFloor to the maxArea on random sides.
   *
   * @param layout The layout of the level.
   * @param maxArea Maximum area of the room on which FloorTiles can be placed.
   * @param baseFloorPadding Padding of the baseFloor.
   * @param baseFloor Area of the baseFloor.
   */
  private void extendToSides(
      final LevelElement[][] layout,
      final Area maxArea,
      final Area baseFloorPadding,
      final Area baseFloor) {
    boolean north = false;
    boolean south = false;
    boolean west = false;
    boolean east = false;
    if (random.nextFloat() < PROBABILITY_SIDE) {
      north = true;
    }
    if (random.nextFloat() < PROBABILITY_SIDE) {
      south = true;
    }
    if (random.nextFloat() < PROBABILITY_SIDE) {
      west = true;
    }
    if (random.nextFloat() < PROBABILITY_SIDE) {
      east = true;
    }
    if (north) {
      for (int y = WALL_BUFFER + baseFloorPadding.y + baseFloor.y;
          y < WALL_BUFFER + maxArea.y;
          y++) {
        for (int x = WALL_BUFFER + baseFloorPadding.x;
            x < WALL_BUFFER + baseFloorPadding.x + baseFloor.x;
            x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
    if (south) {
      for (int y = WALL_BUFFER; y < WALL_BUFFER + baseFloorPadding.y; y++) {
        for (int x = WALL_BUFFER + baseFloorPadding.x;
            x < WALL_BUFFER + baseFloorPadding.x + baseFloor.x;
            x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
    if (west) {
      for (int y = WALL_BUFFER + baseFloorPadding.y;
          y < WALL_BUFFER + baseFloorPadding.y + baseFloor.y;
          y++) {
        for (int x = WALL_BUFFER; x < WALL_BUFFER + baseFloorPadding.x; x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
    if (east) {
      for (int y = WALL_BUFFER + baseFloorPadding.y;
          y < WALL_BUFFER + baseFloorPadding.y + baseFloor.y;
          y++) {
        for (int x = WALL_BUFFER + baseFloorPadding.x + baseFloor.x;
            x < WALL_BUFFER + maxArea.x;
            x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
  }

  /**
   * Extends the baseFloor to the maxArea in random corners.
   *
   * @param layout The layout of the level.
   * @param maxArea Maximum area of the room on which FloorTiles can be placed.
   * @param baseFloorPadding Padding of the baseFloor.
   * @param baseFloor Area of the baseFloor.
   */
  private void extendToCorners(
      final LevelElement[][] layout,
      final Area maxArea,
      final Area baseFloorPadding,
      final Area baseFloor) {
    boolean upperLeft = false;
    boolean upperRight = false;
    boolean lowerLeft = false;
    boolean lowerRight = false;
    int i = WALL_BUFFER + baseFloorPadding.y + baseFloor.y - baseFloor.y / 2 + 1;
    if (random.nextFloat() < PROBABILITY_CORNER) {
      upperLeft = true;
    }
    if (random.nextFloat() < PROBABILITY_CORNER) {
      upperRight = true;
    }
    if (random.nextFloat() < PROBABILITY_CORNER) {
      lowerLeft = true;
    }
    if (random.nextFloat() < PROBABILITY_CORNER) {
      lowerRight = true;
    }
    if (upperLeft) {
      for (int y = i; y < WALL_BUFFER + maxArea.y; y++) {
        for (int x = WALL_BUFFER; x < WALL_BUFFER + baseFloorPadding.x + baseFloor.x / 2 - 1; x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
    if (upperRight) {
      for (int y = i; y < WALL_BUFFER + maxArea.y; y++) {
        for (int x = i; x < WALL_BUFFER + maxArea.x; x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
    if (lowerLeft) {
      for (int y = WALL_BUFFER; y < WALL_BUFFER + baseFloorPadding.y + baseFloor.y / 2 - 1; y++) {
        for (int x = WALL_BUFFER; x < WALL_BUFFER + baseFloorPadding.x + baseFloor.x / 2 - 1; x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
    if (lowerRight) {
      for (int y = WALL_BUFFER; y < WALL_BUFFER + baseFloorPadding.y + baseFloor.y / 2 - 1; y++) {
        for (int x = WALL_BUFFER + baseFloorPadding.x + baseFloor.x - baseFloor.x / 2 + 1;
            x < WALL_BUFFER + maxArea.x;
            x++) {
          layout[y][x] = LevelElement.FLOOR;
        }
      }
    }
  }

  /**
   * Adds four 3x3 support columns to the room layout.
   *
   * @param layout The layout of the level.
   * @param baseFloorPadding Padding of the baseFloor.
   * @param baseFloor Area of the baseFloor.
   */
  private void addSupports(
      final LevelElement[][] layout, final Area baseFloorPadding, final Area baseFloor) {
    int leftSupportLeftSide = WALL_BUFFER + baseFloorPadding.x + baseFloor.x / 5;
    int leftSupportRightSide = WALL_BUFFER + baseFloorPadding.x + baseFloor.x / 5 + 2;
    int bottomSupportBottomSide = WALL_BUFFER + baseFloorPadding.y + baseFloor.y / 5;
    int bottomSupportUpperSide = WALL_BUFFER + baseFloorPadding.y + baseFloor.y / 5 + 2;
    int rightSupportLeftSide = WALL_BUFFER + baseFloorPadding.x + baseFloor.x - baseFloor.x / 5 - 3;
    int rightSupportRightSide =
        WALL_BUFFER + baseFloorPadding.x + baseFloor.x - baseFloor.x / 5 - 1;
    int upperSupportBottomSide =
        WALL_BUFFER + baseFloorPadding.y + baseFloor.y - baseFloor.y / 5 - 3;
    int upperSupportUpperSide =
        WALL_BUFFER + baseFloorPadding.y + baseFloor.y - baseFloor.y / 5 - 1;

    // lower left support
    for (int y = bottomSupportBottomSide; y <= bottomSupportUpperSide; y++) {
      for (int x = leftSupportLeftSide; x <= leftSupportRightSide; x++) {
        if (y == bottomSupportBottomSide + 1 && x == leftSupportLeftSide + 1) {
          layout[y][x] = LevelElement.SKIP;
        } else {
          layout[y][x] = LevelElement.WALL;
        }
      }
    }

    // lower right support
    for (int y = bottomSupportBottomSide; y <= bottomSupportUpperSide; y++) {
      for (int x = rightSupportLeftSide; x <= rightSupportRightSide; x++) {
        if (y == bottomSupportBottomSide + 1 && x == rightSupportLeftSide + 1) {
          layout[y][x] = LevelElement.SKIP;
        } else {
          layout[y][x] = LevelElement.WALL;
        }
      }
    }

    // upper left support
    for (int y = upperSupportBottomSide; y <= upperSupportUpperSide; y++) {
      for (int x = leftSupportLeftSide; x <= leftSupportRightSide; x++) {
        if (y == upperSupportBottomSide + 1 && x == leftSupportLeftSide + 1) {
          layout[y][x] = LevelElement.SKIP;
        } else {
          layout[y][x] = LevelElement.WALL;
        }
      }
    }

    // upper right support
    for (int y = upperSupportBottomSide; y <= upperSupportUpperSide; y++) {
      for (int x = rightSupportLeftSide; x <= rightSupportRightSide; x++) {
        if (y == upperSupportBottomSide + 1 && x == rightSupportLeftSide + 1) {
          layout[y][x] = LevelElement.SKIP;
        } else {
          layout[y][x] = LevelElement.WALL;
        }
      }
    }
  }

  /**
   * Adds doors to the room layout.
   *
   * @param doors Array of DoorDirections to specify where doors should be generated.
   * @param maxArea Maximum area of the room on which FloorTiles can be placed.
   * @param layout The layout of the level.
   * @param size Size of the level.
   */
  private void addDoors(
      final LevelNode[] doors,
      final Area maxArea,
      final LevelElement[][] layout,
      final LevelSize size) {
    boolean north = doors[Direction.NORTH.value()] != null;
    boolean south = doors[Direction.SOUTH.value()] != null;
    boolean west = doors[Direction.WEST.value()] != null;
    boolean east = doors[Direction.EAST.value()] != null;

    if (north) {
      ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
      for (int y = WALL_BUFFER + maxArea.y; y > WALL_BUFFER; y--) {
        for (int x = WALL_BUFFER; x < WALL_BUFFER + maxArea.x; x++) {
          // only mark walls that are not next to a corner
          if (layout[y][x] == LevelElement.WALL
              && layout[y - 1][x - 1] != LevelElement.WALL
              && layout[y - 1][x + 1] != LevelElement.WALL
              && layout[y][x - 1] == LevelElement.WALL
              && layout[y][x + 1] == LevelElement.WALL) {
            possibleDoorCoordinates.add(new Coordinate(x, y));
          }
        }
        if (!possibleDoorCoordinates.isEmpty()) {
          break;
        }
      }
      if (possibleDoorCoordinates.isEmpty())
        throw new CantPlaceDoorException(layout, Direction.NORTH, size);
      int doorIndex = random.nextInt(possibleDoorCoordinates.size());
      Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
      layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
      // make sure the door is accessible
      layout[doorCoordinate.y - 1][doorCoordinate.x] = LevelElement.FLOOR;
    }
    if (south) {
      ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
      for (int y = WALL_BUFFER - 1; y < WALL_BUFFER + maxArea.y; y++) {
        for (int x = WALL_BUFFER; x < WALL_BUFFER + maxArea.x; x++) {
          // only mark walls that are not next to a corner
          if (layout[y][x] == LevelElement.WALL
              && layout[y + 1][x - 1] != LevelElement.WALL
              && layout[y + 1][x + 1] != LevelElement.WALL
              && layout[y][x - 1] == LevelElement.WALL
              && layout[y][x + 1] == LevelElement.WALL) {
            possibleDoorCoordinates.add(new Coordinate(x, y));
          }
        }
        if (!possibleDoorCoordinates.isEmpty()) {
          break;
        }
      }
      if (possibleDoorCoordinates.isEmpty())
        throw new CantPlaceDoorException(layout, Direction.SOUTH, size);
      int doorIndex = random.nextInt(possibleDoorCoordinates.size());
      Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
      layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
      // make sure the door is accessible
      layout[doorCoordinate.y + 1][doorCoordinate.x] = LevelElement.FLOOR;
    }
    if (west) {
      ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
      for (int x = WALL_BUFFER - 1; x < WALL_BUFFER + maxArea.x; x++) {
        for (int y = WALL_BUFFER; y < WALL_BUFFER + maxArea.y; y++) {
          // only mark walls that are not next to a corner
          if (layout[y][x] == LevelElement.WALL
              && layout[y - 1][x + 1] != LevelElement.WALL
              && layout[y + 1][x + 1] != LevelElement.WALL
              && layout[y - 1][x] == LevelElement.WALL
              && layout[y + 1][x] == LevelElement.WALL) {
            possibleDoorCoordinates.add(new Coordinate(x, y));
          }
        }
        if (!possibleDoorCoordinates.isEmpty()) {
          break;
        }
      }
      if (possibleDoorCoordinates.isEmpty())
        throw new CantPlaceDoorException(layout, Direction.WEST, size);
      int doorIndex = random.nextInt(possibleDoorCoordinates.size());
      Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
      layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
      // make sure the door is accessible
      layout[doorCoordinate.y][doorCoordinate.x + 1] = LevelElement.FLOOR;
    }
    if (east) {
      ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
      for (int x = WALL_BUFFER + maxArea.x; x > WALL_BUFFER; x--) {
        for (int y = WALL_BUFFER; y < WALL_BUFFER + maxArea.y; y++) {
          // only mark walls that are not next to a corner
          if (layout[y][x] == LevelElement.WALL
              && layout[y - 1][x - 1] != LevelElement.WALL
              && layout[y + 1][x - 1] != LevelElement.WALL
              && layout[y - 1][x] == LevelElement.WALL
              && layout[y + 1][x] == LevelElement.WALL) {
            possibleDoorCoordinates.add(new Coordinate(x, y));
          }
        }
        if (!possibleDoorCoordinates.isEmpty()) {
          break;
        }
      }
      if (possibleDoorCoordinates.isEmpty())
        throw new CantPlaceDoorException(layout, Direction.EAST, size);
      int doorIndex = random.nextInt(possibleDoorCoordinates.size());
      Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
      layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
      // make sure the door is accessible
      layout[doorCoordinate.y][doorCoordinate.x - 1] = LevelElement.FLOOR;
    }
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
