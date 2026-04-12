package core.level;

import static org.junit.jupiter.api.Assertions.*;

import core.Game;
import core.game.ECSManagement;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.TileFactory;
import core.level.elements.tile.WallTile;
import core.level.loader.parsers.V2FormatParser;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.platform.Platform;
import core.level.path.GridPathfindingAdapter;
import core.systems.LevelSystem;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link DungeonLevel} class.
 *
 * <p>This test class covers the core functionality of the DungeonLevel including:
 * <ul>
 *   <li>Level construction from tiles and level elements
 *   <li>Pathfinding on level layouts
 *   <li>Tile retrieval and random tile selection
 *   <li>Tile management (adding, removing, and changing tiles)
 *   <li>Level serialization
 * </ul>
 */
public class DungeonLevelTest {

  /**
   * Sets up a neutral runtime environment for each test.
   *
   * <p>Resets the ECS management system, clears all entities and systems, initializes a fresh
   * LevelSystem, and configures grid-based pathfinding for consistent test execution.
   */
  @BeforeEach
  void setupNeutralRuntime() {
    ECSManagement.removeAllEntities();
    ECSManagement.removeAllSystems();
    ECSManagement.add(new LevelSystem());
    Platform.pathfinding(new GridPathfindingAdapter());
  }

  /**
   * Helper method to find a path between two tiles using the pathfinding system.
   *
   * @param level the dungeon level to perform pathfinding on
   * @param start the starting tile
   * @param end the end (target) tile
   * @return a list of tiles representing the path from start to end
   */
  private static List<Tile> findPath(DungeonLevel level, Tile start, Tile end) {
    Game.currentLevel(level);
    return Game.findPath(start, end).orElseThrow();
  }

  /**
   * Tests the construction of a DungeonLevel from a 2D array of Tile objects.
   *
   * <p>Verifies that the level correctly stores and retrieves the provided tile layout.
   */
  @Test
  public void test_levelCTOR_Tiles() {
    Tile[][] tileLayout =
      new Tile[][] {
        {
          new WallTile(new SimpleIPath(""), new Coordinate(0, 0), DesignLabel.DEFAULT),
          new FloorTile(new SimpleIPath(""), new Coordinate(1, 0), DesignLabel.DEFAULT)
        },
        {
          new WallTile(new SimpleIPath(""), new Coordinate(0, 1), DesignLabel.DEFAULT),
          new ExitTile(new SimpleIPath(""), new Coordinate(1, 1), DesignLabel.DEFAULT)
        }
      };
    DungeonLevel tileLevel = new DungeonLevel(tileLayout);
    Tile[][] layout = tileLevel.layout();
    assertArrayEquals(tileLayout, layout);
  }

  /**
   * Tests the construction of a DungeonLevel from a 2D array of LevelElement values.
   *
   * <p>Verifies that LevelElements are correctly converted to appropriate Tile objects during
   * level construction.
   */
  @Test
  public void test_levelCTOR_LevelElements() {
    LevelElement[][] elementsLayout =
      new LevelElement[][] {
        {LevelElement.WALL, LevelElement.FLOOR}, {LevelElement.WALL, LevelElement.EXIT}
      };
    DungeonLevel tileLevel = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT);
    Tile[][] layout = tileLevel.layout();
    assertSame(elementsLayout[0][0], layout[0][0].levelElement());
    assertSame(elementsLayout[1][0], layout[1][0].levelElement());
    assertSame(elementsLayout[0][1], layout[0][1].levelElement());
    assertSame(elementsLayout[1][1], layout[1][1].levelElement());
  }

  /**
   * Tests the construction of a DungeonLevel from LevelElements and verifies that tile type
   * lists are properly populated.
   *
   * <p>Verifies that the level correctly categorizes tiles into their respective type lists
   * (floors, doors, holes, walls, and skips).
   */
  @Test
  public void test_levelCTOR_LevelElements_tileTypeLists() {
    LevelElement[][] elementsLayout =
      new LevelElement[][] {
        {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.EXIT, LevelElement.SKIP},
        {LevelElement.WALL, LevelElement.WALL, LevelElement.SKIP, LevelElement.SKIP},
        {LevelElement.DOOR, LevelElement.DOOR, LevelElement.HOLE, LevelElement.HOLE},
      };
    DungeonLevel tileLevel = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT);
    assertEquals(2, tileLevel.floorTiles().size());
    assertEquals(2, tileLevel.doorTiles().size());
    assertEquals(2, tileLevel.holeTiles().size());
    assertEquals(2, tileLevel.wallTiles().size());
    assertEquals(3, tileLevel.skipTiles().size());
  }

  /**
   * Tests pathfinding from a start position to an end position when only one valid path exists.
   *
   * <p>Creates a 3x3 level with walls configured so that only one unique path is possible from
   * the start to the exit. Verifies that the pathfinding algorithm finds the correct path with
   * the expected number of steps.
   */
  @Test
  public void test_findPath_onlyOnePathPossible() {
    Tile[][] layout = new Tile[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        layout[y][x] =
          new FloorTile(new SimpleIPath(""), new Coordinate(x, y), DesignLabel.DEFAULT);
      }
    }
    layout[1][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 1), DesignLabel.DEFAULT);
    layout[0][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 0), DesignLabel.DEFAULT);
    layout[0][2] = new ExitTile(new SimpleIPath(""), new Coordinate(2, 0), DesignLabel.DEFAULT);

    DungeonLevel tileLevel = new DungeonLevel(layout);
    tileLevel.startTiles().add(layout[0][0]);

    List<Tile> path =
      findPath(tileLevel, tileLevel.startTile().orElseThrow(), tileLevel.endTile().orElseThrow());

    assertEquals(7, path.size());
    assertEquals(layout[0][0], path.get(0));
    assertEquals(layout[1][0], path.get(1));
    assertEquals(layout[2][0], path.get(2));
    assertEquals(layout[2][1], path.get(3));
    assertEquals(layout[2][2], path.get(4));
    assertEquals(layout[1][2], path.get(5));
    assertEquals(layout[0][2], path.get(6));
  }

  /**
   * Tests pathfinding from a start position to an end position when multiple valid paths exist.
   *
   * <p>Creates a 3x3 level where multiple paths from start to exit are possible. Verifies that
   * the pathfinding algorithm finds a valid path (not necessarily the shortest one) that
   * successfully connects the start and end positions.
   */
  @Test
  public void test_findPath_moreThanOnePathPossible() {
    Tile[][] layout = new Tile[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        layout[y][x] =
          new FloorTile(new SimpleIPath(""), new Coordinate(x, y), DesignLabel.DEFAULT);
      }
    }
    layout[0][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 0), DesignLabel.DEFAULT);
    layout[0][2] = new ExitTile(new SimpleIPath(""), new Coordinate(2, 0), DesignLabel.DEFAULT);

    DungeonLevel tileLevel = new DungeonLevel(layout);
    tileLevel.startTiles().add(layout[0][0]);

    List<Tile> path =
      findPath(tileLevel, tileLevel.startTile().orElseThrow(), tileLevel.endTile().orElseThrow());

    assertEquals(5, path.size());
    assertEquals(layout[0][0], path.get(0));
    assertEquals(layout[1][0], path.get(1));
    assertEquals(layout[1][1], path.get(2));
    assertEquals(layout[1][2], path.get(3));
    assertEquals(layout[0][2], path.get(4));
  }

  /**
   * Tests pathfinding in a level containing skip tiles.
   *
   * <p>Verifies that the pathfinding algorithm correctly handles skip tiles (non-accessible
   * tiles that can be passed through) and finds the correct path between two positions.
   */
  @Test
  public void test_findPath_withSkips() {
    var levelElement = new LevelElement[3][2];
    for (int i = 0; i < 3; i++) {
      levelElement[i][0] = LevelElement.SKIP;
    }
    for (int i = 0; i < 3; i++) {
      levelElement[i][1] = LevelElement.FLOOR;
    }
    var level = new DungeonLevel(levelElement, DesignLabel.DEFAULT);
    var start = level.tileAt(new Coordinate(1, 0)).orElseThrow();
    var end = level.tileAt(new Coordinate(1, 2)).orElseThrow();
    var path = findPath(level, end, start);
    assertEquals(3, path.size());
  }

  /**
   * Tests pathfinding in a level without skip tiles.
   *
   * <p>Verifies that the pathfinding algorithm correctly calculates the path length in a
   * simple level containing only floor tiles.
   */
  @Test
  public void test_findPath_withoutSkips() {
    var levelElement = new LevelElement[3][1];

    for (int i = 0; i < 3; i++) {
      levelElement[i][0] = LevelElement.FLOOR;
    }
    var level = new DungeonLevel(levelElement, DesignLabel.DEFAULT);
    var start = level.tileAt(new Coordinate(0, 0)).orElseThrow();
    var end = level.tileAt(new Coordinate(0, 2)).orElseThrow();
    var path = findPath(level, end, start);
    assertEquals(3, path.size());
  }

  /**
   * Tests pathfinding when the start position is not accessible (is a wall tile).
   *
   * <p>Verifies that the pathfinding algorithm correctly returns an empty path when the start
   * position is a non-accessible tile.
   */
  @Test
  public void test_findPath_startPositionNotAccessible() {
    Tile[][] layout = new Tile[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        layout[y][x] =
          new FloorTile(new SimpleIPath(""), new Coordinate(x, y), DesignLabel.DEFAULT);
      }
    }
    layout[0][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 0), DesignLabel.DEFAULT);
    DungeonLevel tileLevel = new DungeonLevel(layout);
    Tile startTile = tileLevel.tileAt(layout[0][1].coordinate()).orElseThrow();
    Tile endTile = tileLevel.tileAt(layout[2][1].coordinate()).orElseThrow();

    List<Tile> path = findPath(tileLevel, startTile, endTile);
    assertTrue(path.isEmpty());
  }

  /**
   * Tests pathfinding when the end position is not accessible (is a wall tile).
   *
   * <p>Verifies that the pathfinding algorithm correctly returns an empty path when the end
   * position is a non-accessible tile.
   */
  @Test
  public void test_findPath_endPositionNotAccessible() {
    Tile[][] layout = new Tile[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        layout[y][x] =
          new FloorTile(new SimpleIPath(""), new Coordinate(x, y), DesignLabel.DEFAULT);
      }
    }
    layout[2][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 2), DesignLabel.DEFAULT);
    DungeonLevel tileLevel = new DungeonLevel(layout);
    Tile startTile = tileLevel.tileAt(layout[0][1].coordinate()).orElseThrow();
    Tile endTile = tileLevel.tileAt(layout[2][1].coordinate()).orElseThrow();

    List<Tile> path = findPath(tileLevel, startTile, endTile);
    assertTrue(path.isEmpty());
  }

  /**
   * Tests pathfinding when both the start and end positions are not accessible.
   *
   * <p>Verifies that the pathfinding algorithm correctly returns an empty path when both the
   * start and end positions are non-accessible tiles.
   */
  @Test
  public void test_findPath_startAndEndPositionNotAccessible() {
    Tile[][] layout = new Tile[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        layout[y][x] =
          new FloorTile(new SimpleIPath(""), new Coordinate(x, y), DesignLabel.DEFAULT);
      }
    }
    layout[0][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 0), DesignLabel.DEFAULT);
    layout[2][1] = new WallTile(new SimpleIPath(""), new Coordinate(1, 2), DesignLabel.DEFAULT);
    DungeonLevel tileLevel = new DungeonLevel(layout);
    Tile startTile = tileLevel.tileAt(layout[0][1].coordinate()).orElseThrow();
    Tile endTile = tileLevel.tileAt(layout[2][1].coordinate()).orElseThrow();

    List<Tile> path = findPath(tileLevel, startTile, endTile);
    assertTrue(path.isEmpty());
  }

  /**
   * Tests the retrieval of a tile at a specific coordinate.
   *
   * <p>Verifies that {@link DungeonLevel#tileAt(core.level.utils.Coordinate)} returns the
   * correct tile at the specified coordinate position.
   */
  @Test
  public void test_getTileAt() {
    var levelLayout = new LevelElement[3][3];

    for (int y = 0; y < 3; y++) {
      Arrays.fill(levelLayout[y], LevelElement.FLOOR);
    }
    levelLayout[0][0] = LevelElement.EXIT;
    var level = new DungeonLevel(levelLayout, DesignLabel.DEFAULT);
    assertEquals(
      levelLayout[1][2], level.tileAt(new Coordinate(2, 1)).orElseThrow().levelElement());
  }

  /**
   * Tests retrieval of a random tile from the level.
   *
   * <p>Verifies that {@link DungeonLevel#randomTile()} returns a non-null tile from the level.
   */
  @Test
  public void test_getRandomTile() {
    var levelLayout = new LevelElement[3][3];
    for (int y = 0; y < 3; y++) {
      Arrays.fill(levelLayout[y], LevelElement.FLOOR);
    }
    var level = new DungeonLevel(levelLayout, DesignLabel.DEFAULT);
    assertNotNull(level.randomTile());
  }

  /**
   * Tests retrieval of a random tile of a specific element type from the level.
   *
   * <p>Verifies that {@link DungeonLevel#randomTile()} returns a random tile that matches the
   * specified element type.
   */
  @Test
  public void test_getRandomTile_WithElementType() {
    LevelElement[][] layout = new LevelElement[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        if (x < 2) {
          layout[y][x] = LevelElement.FLOOR;
        } else {
          layout[y][x] = LevelElement.WALL;
        }
      }
    }
    layout[2][1] = LevelElement.EXIT;

    DungeonLevel tileLevel = new DungeonLevel(layout, DesignLabel.DEFAULT);

    Point randomWallPoint = tileLevel.randomTilePoint(LevelElement.WALL).orElseThrow();
    assertNotNull(randomWallPoint);
    Tile randomWall = tileLevel.tileAt(randomWallPoint).orElseThrow();
    assertNotNull(randomWall);
    assertEquals(LevelElement.WALL, randomWall.levelElement());
  }

  /**
   * Tests retrieval of a random tile point (coordinate) from the level.
   *
   * <p>Verifies that {@link DungeonLevel#randomTilePoint()} returns a valid coordinate point
   * within the level layout.
   */
  @Test
  public void test_getRandomTilePoint() {
    var levelLayout = new LevelElement[3][3];
    for (int y = 0; y < 3; y++) {
      Arrays.fill(levelLayout[y], LevelElement.FLOOR);
    }
    var level = new DungeonLevel(levelLayout, DesignLabel.DEFAULT);
    Point randomPoint = level.randomTilePoint().orElse(null);
    assertNotNull(randomPoint);
    assertTrue(level.tileAt(randomPoint).isPresent());
  }

  /**
   * Tests retrieval of a random tile point of a specific element type from the level.
   *
   * <p>Verifies that {@link DungeonLevel#randomTilePoint(LevelElement)} returns a valid
   * coordinate of a tile matching the specified element type.
   */
  @Test
  public void test_getRandomTilePoint_WithElementType() {
    LevelElement[][] layout = new LevelElement[3][3];
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        if (x < 2) {
          layout[y][x] = LevelElement.FLOOR;
        } else {
          layout[y][x] = LevelElement.WALL;
        }
      }
    }
    layout[2][1] = LevelElement.EXIT;

    DungeonLevel tileLevel = new DungeonLevel(layout, DesignLabel.DEFAULT);

    Point randomWallPoint = tileLevel.randomTilePoint(LevelElement.WALL).orElseThrow();
    Point randomFloorPoint = tileLevel.randomTilePoint(LevelElement.FLOOR).orElseThrow();
    Tile randomWall = tileLevel.tileAt(randomWallPoint).orElseThrow();
    Tile randomFloor = tileLevel.tileAt(randomFloorPoint).orElseThrow();
    assertEquals(LevelElement.WALL, randomWall.levelElement());
    assertEquals(LevelElement.FLOOR, randomFloor.levelElement());
  }

  /**
   * Tests the string representation of a level.
   *
   * <p>Verifies that {@link DungeonLevel#toString()} correctly serializes the level layout into
   * a string format that matches the expected level representation.
   */
  @Test
  public void test_toString() {
    LevelElement[][] tileLayout =
      new LevelElement[][] {
        new LevelElement[] {
          LevelElement.WALL, LevelElement.FLOOR,
        },
        new LevelElement[] {
          LevelElement.EXIT, LevelElement.WALL,
        }
      };
    var level = new DungeonLevel(tileLayout, DesignLabel.DEFAULT);
    List<String> lines = new ArrayList<>();
    for (LevelElement[] tiles : tileLayout) {
      StringBuilder row = new StringBuilder();
      for (LevelElement tile : tiles) {
        if (tile == LevelElement.FLOOR) {
          row.append("F");
        } else if (tile == LevelElement.WALL) {
          row.append("W");
        } else {
          row.append("E");
        }
      }
      lines.add(row.toString());
    }
    lines = lines.reversed();
    String compareString = String.join(System.lineSeparator(), lines);
    assertEquals(compareString, V2FormatParser.serializeLevelLayout(level.layout));
  }

  /**
   * Tests adding a floor tile to the level.
   *
   * <p>Verifies that a floor tile can be added to the level correctly and that it is contained
   * in the floor tiles list, associated with the correct level, and counted as an accessible
   * tile.
   */
  @Test
  public void test_addTile_FloorTile() {
    DungeonLevel level =
      new DungeonLevel(
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}},
        DesignLabel.DEFAULT);
    Tile tile =
      TileFactory.createTile(
        new SimpleIPath(""), new Coordinate(1, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
    level.removeTile(level.layout()[0][1]);
    level.layout()[0][1] = tile;
    level.addTile(tile);
    assertTrue(level.floorTiles().contains(tile));
    assertSame(level, tile.level());
    assertEquals(
      3,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());
  }

  /**
   * Tests adding an exit tile to the level.
   *
   * <p>Verifies that an exit tile can be added to the level correctly and that it is contained
   * in the exit tiles list, associated with the correct level, and counted as an accessible
   * tile.
   */
  @Test
  public void test_addTile_ExitTile() {
    DungeonLevel level =
      new DungeonLevel(
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}},
        DesignLabel.DEFAULT);
    Tile tile =
      TileFactory.createTile(
        new SimpleIPath(""), new Coordinate(1, 0), LevelElement.EXIT, DesignLabel.DEFAULT);
    level.removeTile(level.layout()[0][1]);
    level.layout()[0][1] = tile;
    level.addTile(tile);
    assertTrue(level.exitTiles().contains(tile));
    assertSame(level, tile.level());
    assertEquals(
      3,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());
  }

  /**
   * Tests adding a door tile to the level.
   *
   * <p>Verifies that a door tile can be added to the level correctly and that it is contained
   * in the door tiles list, associated with the correct level, and counted as an accessible
   * tile.
   */
  @Test
  public void test_addTile_DoorTile() {
    DungeonLevel level =
      new DungeonLevel(
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}},
        DesignLabel.DEFAULT);
    Tile tile =
      TileFactory.createTile(
        new SimpleIPath(".png"), new Coordinate(1, 0), LevelElement.DOOR, DesignLabel.DEFAULT);
    level.removeTile(level.layout()[0][1]);
    level.layout()[0][1] = tile;
    level.addTile(tile);
    assertTrue(level.doorTiles().contains(tile));
    assertSame(level, tile.level());
    assertEquals(
      3,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());
  }

  /**
   * Tests adding a skip tile to the level.
   *
   * <p>Verifies that a skip tile can be added to the level correctly and that it is contained
   * in the skip tiles list, associated with the correct level, and correctly handled in
   * accessibility calculations.
   */
  @Test
  public void test_addTile_SkipTile() {
    DungeonLevel level =
      new DungeonLevel(
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}},
        DesignLabel.DEFAULT);
    Tile tile =
      TileFactory.createTile(
        new SimpleIPath(""), new Coordinate(1, 0), LevelElement.SKIP, DesignLabel.DEFAULT);
    level.removeTile(level.layout()[0][1]);
    level.layout()[0][1] = tile;
    level.addTile(tile);
    assertTrue(level.skipTiles().contains(tile));
    assertSame(level, tile.level());
    assertEquals(
      2,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());
  }

  /**
   * Tests adding a wall tile to the level.
   *
   * <p>Verifies that a wall tile can be added to the level correctly and that it is contained
   * in the wall tiles list, associated with the correct level, and correctly affects the
   * count of accessible tiles.
   */
  @Test
  public void test_addTile_WallTile() {
    DungeonLevel level =
      new DungeonLevel(
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}},
        DesignLabel.DEFAULT);
    Tile tile =
      TileFactory.createTile(
        new SimpleIPath(""), new Coordinate(1, 0), LevelElement.WALL, DesignLabel.DEFAULT);
    level.removeTile(level.layout()[0][1]);
    level.layout()[0][1] = tile;
    level.addTile(tile);
    assertTrue(level.wallTiles().contains(tile));
    assertSame(level, tile.level());
    assertEquals(
      2,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());
  }

  /**
   * Tests adding a hole tile to the level.
   *
   * <p>Verifies that a hole tile can be added to the level correctly and that it is contained
   * in the hole tiles list, associated with the correct level, and correctly affects the
   * count of accessible tiles.
   */
  @Test
  public void test_addTile_HoleTile() {
    DungeonLevel level =
      new DungeonLevel(
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}},
        DesignLabel.DEFAULT);
    Tile tile =
      TileFactory.createTile(
        new SimpleIPath(""), new Coordinate(1, 0), LevelElement.HOLE, DesignLabel.DEFAULT);
    level.removeTile(level.layout()[0][1]);
    level.layout()[0][1] = tile;
    level.addTile(tile);
    assertTrue(level.holeTiles().contains(tile));
    assertSame(level, tile.level());
    assertEquals(
      2,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());
  }

  /**
   * Tests changing a tile element type when the new type is the same as the current one.
   *
   * <p>Verifies that changing a tile to the same element type maintains the level's tile count
   * and accessibility properties without causing inconsistencies.
   */
  @Test
  public void test_changeTileElementType_SameElementType() {
    LevelElement[][] layout =
      new LevelElement[][] {
        new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
      };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
      level.tileAt(new Coordinate(0, 0)).orElseThrow(), LevelElement.FLOOR);

    assertEquals(
      3,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());

    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
      .flatMap(Arrays::stream)
      .sorted(Comparator.comparingInt(Tile::index))
      .filter(Tile::isAccessible)
      .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));
    assertEquals(3, counter.get());
  }

  /**
   * Tests changing a tile element type to another accessible type.
   *
   * <p>Verifies that changing a floor tile to an exit tile (both accessible) maintains the
   * level's tile count and accessibility properties.
   */
  @Test
  public void test_changeTileElementType_SameAccess() {
    LevelElement[][] layout =
      new LevelElement[][] {
        new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
      };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
      level.tileAt(new Coordinate(0, 0)).orElseThrow(), LevelElement.EXIT);

    assertEquals(
      3,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());

    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
      .flatMap(Arrays::stream)
      .filter(Tile::isAccessible)
      .sorted(Comparator.comparingInt(Tile::index))
      .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));
    assertEquals(3, counter.get());
  }

  /**
   * Tests changing a tile element type from an accessible type to a non-accessible type.
   *
   * <p>Verifies that changing a floor tile to a wall tile properly reduces the count of
   * accessible tiles and maintains correct tile indexing.
   */
  @Test
  public void test_changeTileElementType_toNotAccessible() {
    LevelElement[][] layout =
      new LevelElement[][] {
        new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
      };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
      level.tileAt(new Coordinate(0, 0)).orElseThrow(), LevelElement.WALL);

    assertEquals(
      2,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());

    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
      .flatMap(Arrays::stream)
      .sorted(Comparator.comparingInt(Tile::index))
      .filter(Tile::isAccessible)
      .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));
    assertEquals(2, counter.get());
  }

  /**
   * Tests changing a tile element type for a tile that is not part of the level.
   *
   * <p>Verifies that attempting to change the element type of tile that is not in the level's
   * layout has no effect on the level state. The level remains unchanged and the tile at the
   * specified coordinate retains its original element type.
   */
  @Test
  public void test_changeTileElementType_notOnLevel() {
    LevelElement[][] layout =
      new LevelElement[][] {
        new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
      };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
      TileFactory.createTile(
        new SimpleIPath(""), new Coordinate(1, 0), LevelElement.FLOOR, DesignLabel.DEFAULT),
      LevelElement.WALL);

    assertEquals(
      3,
      Arrays.stream(level.layout()).flatMap(Arrays::stream).filter(Tile::isAccessible).count());

    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
      .flatMap(Arrays::stream)
      .sorted(Comparator.comparingInt(Tile::index))
      .filter(Tile::isAccessible)
      .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));

    assertNotEquals(
      LevelElement.WALL, level.tileAt(new Coordinate(1, 0)).orElseThrow().levelElement());
    assertEquals(3, counter.get());
  }
}
