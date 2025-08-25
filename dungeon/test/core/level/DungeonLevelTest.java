package core.level;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.ai.pfa.GraphPath;
import core.level.elements.astar.TileConnection;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.TileFactory;
import core.level.elements.tile.WallTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Tests for the {@link DungeonLevel} class. */
public class DungeonLevelTest {
  /** WTF? . */
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

  /** WTF? . */
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

  /** WTF? . */
  @Test
  public void test_levelCTOR_LevelElements_connections() {
    LevelElement[][] elementsLayout =
        new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.EXIT}};
    DungeonLevel tileLevel = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT);
    Tile[][] layout = tileLevel.layout();
    assertEquals(1, layout[0][0].connections().size);
    assertSame(layout[0][1], layout[0][0].connections().first().getToNode());
    assertEquals(2, layout[0][1].connections().size);
    assertSame(layout[0][0], layout[0][1].connections().get(0).getToNode());
    assertSame(layout[0][2], layout[0][1].connections().get(1).getToNode());
    assertEquals(1, layout[0][2].connections().size);
    assertSame(layout[0][1], layout[0][2].connections().first().getToNode());
  }

  /** WTF? . */
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

  /** WTF? . */
  @Test
  public void test_nodeCount_NoAccessible() {
    LevelElement[][] elementsLayout =
        new LevelElement[][] {
          {LevelElement.FLOOR, LevelElement.WALL, LevelElement.WALL, LevelElement.WALL},
        };
    DungeonLevel tileLevel = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT);
    tileLevel.changeTileElementType(tileLevel.floorTiles().getFirst(), LevelElement.WALL);
    assertEquals(0, tileLevel.getNodeCount());
  }

  /** WTF? . */
  @Test
  public void test_nodeCount_OneAccessible() {
    LevelElement[][] elementsLayout =
        new LevelElement[][] {
          {LevelElement.FLOOR, LevelElement.WALL, LevelElement.WALL, LevelElement.WALL},
        };
    DungeonLevel tileLevel = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT);
    assertEquals(1, tileLevel.getNodeCount());
  }

  /** WTF? . */
  @Test
  public void test_nodeCount_FourAccessible() {
    LevelElement[][] elementsLayout =
        new LevelElement[][] {
          {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR},
        };
    DungeonLevel tileLevel = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT);
    assertEquals(4, tileLevel.getNodeCount());
  }

  /** WTF? . */
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
    tileLevel.startTile(layout[0][0]);

    /* How the level layout looks: (S=start, W=Wall,F=Floor,E=exit) SWE FWF FFF */
    GraphPath<Tile> path =
        tileLevel.findPath(tileLevel.startTile().orElseThrow(), tileLevel.endTile().orElseThrow());
    assertEquals(7, path.getCount());
    assertEquals(layout[0][0], path.get(0));
    assertEquals(layout[1][0], path.get(1));
    assertEquals(layout[2][0], path.get(2));
    assertEquals(layout[2][1], path.get(3));
    assertEquals(layout[2][2], path.get(4));
    assertEquals(layout[1][2], path.get(5));
    assertEquals(layout[0][2], path.get(6));
  }

  /** WTF? . */
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
    tileLevel.startTile(layout[0][0]);

    /* How the level layout looks: (S=start, W=Wall,F=Floor,E=exit)
    SWE
    FFF
    FFF */
    // should take the shortest path
    GraphPath<Tile> path =
        tileLevel.findPath(tileLevel.startTile().orElseThrow(), tileLevel.endTile().orElseThrow());
    assertEquals(5, path.getCount());
    assertEquals(layout[0][0], path.get(0));
    assertEquals(layout[1][0], path.get(1));
    assertEquals(layout[1][1], path.get(2));
    assertEquals(layout[1][2], path.get(3));
    assertEquals(layout[0][2], path.get(4));
  }

  /** WTF? . */
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
    var path = level.findPath(end, start);
    assertEquals(3, path.getCount());
  }

  /** WTF? . */
  @Test
  public void test_findPath_withoutSkips() {
    var levelElement = new LevelElement[3][1];

    for (int i = 0; i < 3; i++) {
      levelElement[i][0] = LevelElement.FLOOR;
    }
    var level = new DungeonLevel(levelElement, DesignLabel.DEFAULT);
    var start = level.tileAt(new Coordinate(0, 0)).orElseThrow();
    var end = level.tileAt(new Coordinate(0, 2)).orElseThrow();
    var path = level.findPath(end, start);
    assertEquals(3, path.getCount());
  }

  /** WTF? . */
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

    assertThrows(IllegalArgumentException.class, () -> tileLevel.findPath(startTile, endTile));
  }

  /** WTF? . */
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

    assertThrows(IllegalArgumentException.class, () -> tileLevel.findPath(startTile, endTile));
  }

  /** WTF? . */
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

    assertThrows(IllegalArgumentException.class, () -> tileLevel.findPath(startTile, endTile));
  }

  /** WTF? . */
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

  /** WTF? . */
  @Test
  public void test_getRandomTile() {
    var levelLayout = new LevelElement[3][3];
    for (int y = 0; y < 3; y++) {
      Arrays.fill(levelLayout[y], LevelElement.FLOOR);
    }
    var level = new DungeonLevel(levelLayout, DesignLabel.DEFAULT);
    assertNotNull(level.randomTile());
  }

  /** WTF? . */
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

  /** WTF? . */
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

  /** WTF? . */
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

  /** WTF? . */
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
    StringBuilder compareString = new StringBuilder();
    for (LevelElement[] tiles : tileLayout) {
      for (LevelElement tile : tiles) {
        if (tile == LevelElement.FLOOR) {
          compareString.append("F");
        } else if (tile == LevelElement.WALL) {
          compareString.append("W");
        } else {
          compareString.append("E");
        }
      }
      compareString.append("\n");
    }
    assertEquals(compareString.toString(), level.printLevel());
  }

  /** WTF? . */
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
    assertEquals(level.getNodeCount() - 1, tile.index());
    assertTrue(
        level.floorTiles().stream()
            .filter(x -> !(x == tile))
            .allMatch(
                x ->
                    x.connections().size == 1
                        && x.connections().contains(new TileConnection(x, tile), false)));
    assertSame(level, tile.level());
    assertEquals(
        3,
        Arrays.stream(level.layout())
            .flatMap(x -> Arrays.stream(x).map(Tile::index))
            .distinct()
            .count());
  }

  /** WTF? . */
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
    assertEquals(level.getNodeCount() - 1, tile.index());
    assertTrue(
        level.floorTiles().stream()
            .filter(x -> !(x == tile))
            .allMatch(
                x ->
                    x.connections().size == 1
                        && x.connections().contains(new TileConnection(x, tile), false)));
    assertSame(level, tile.level());
    assertEquals(
        3,
        Arrays.stream(level.layout())
            .flatMap(x -> Arrays.stream(x).map(Tile::index))
            .distinct()
            .count());
  }

  /** WTF? . */
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
    assertEquals(level.getNodeCount() - 1, tile.index());
    assertTrue(
        level.floorTiles().stream()
            .filter(x -> !(x == tile))
            .allMatch(
                x ->
                    x.connections().size == 1
                        && x.connections().contains(new TileConnection(x, tile), false)));
    assertSame(level, tile.level());
    assertEquals(
        3,
        Arrays.stream(level.layout())
            .flatMap(x -> Arrays.stream(x).map(Tile::index))
            .distinct()
            .count());
  }

  /** WTF? . */
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
    assertEquals(0, tile.index());
    assertTrue(
        level.floorTiles().stream()
            .filter(x -> !(x == tile))
            .allMatch(x -> x.connections().size == 0));
    assertSame(level, tile.level());
    assertEquals(
        2,
        Arrays.stream(level.layout())
            .flatMap(x -> Arrays.stream(x).filter(Tile::isAccessible).map(Tile::index))
            .distinct()
            .count());
  }

  /** WTF? . */
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
    assertEquals(0, tile.index());
    assertTrue(
        level.floorTiles().stream()
            .filter(x -> !(x == tile))
            .allMatch(x -> x.connections().size == 0));
    assertSame(level, tile.level());
    assertEquals(
        2,
        Arrays.stream(level.layout())
            .flatMap(x -> Arrays.stream(x).filter(Tile::isAccessible).map(Tile::index))
            .distinct()
            .count());
  }

  /** WTF? . */
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
    assertEquals(0, tile.index());
    assertTrue(
        level.floorTiles().stream()
            .filter(x -> !(x == tile))
            .allMatch(x -> x.connections().size == 0));
    assertSame(level, tile.level());
    assertEquals(
        2,
        Arrays.stream(level.layout())
            .flatMap(x -> Arrays.stream(x).filter(Tile::isAccessible).map(Tile::index))
            .distinct()
            .count());
  }

  /** WTF? . */
  @Test
  public void test_changeTileElementType_SameElementType() {
    LevelElement[][] layout =
        new LevelElement[][] {
          new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
        };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
        level.tileAt(new Coordinate(0, 0)).orElseThrow(), LevelElement.FLOOR);
    assertEquals(3, level.getNodeCount());
    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
        .flatMap(Arrays::stream)
        .sorted(Comparator.comparingInt(Tile::index))
        .filter(Tile::isAccessible)
        .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));
    assertEquals(3, counter.get());
  }

  /** WTF? . */
  @Test
  public void test_changeTileElementType_SameAccess() {
    LevelElement[][] layout =
        new LevelElement[][] {
          new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
        };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
        level.tileAt(new Coordinate(0, 0)).orElseThrow(), LevelElement.EXIT);
    assertEquals(3, level.getNodeCount());
    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
        .flatMap(Arrays::stream)
        .filter(Tile::isAccessible)
        .sorted(Comparator.comparingInt(Tile::index))
        .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));
    assertEquals(3, counter.get());
  }

  /** WTF? . */
  @Test
  public void test_changeTileElementType_toNotAccessible() {
    LevelElement[][] layout =
        new LevelElement[][] {
          new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
        };
    DungeonLevel level = new DungeonLevel(layout, DesignLabel.DEFAULT);
    level.changeTileElementType(
        level.tileAt(new Coordinate(0, 0)).orElseThrow(), LevelElement.WALL);
    assertEquals(2, level.getNodeCount());
    AtomicInteger counter = new AtomicInteger();
    Arrays.stream(level.layout())
        .flatMap(Arrays::stream)
        .sorted(Comparator.comparingInt(Tile::index))
        .filter(Tile::isAccessible)
        .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.index()));
    assertEquals(2, counter.get());
  }

  /** WTF? . */
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
    assertEquals(3, level.getNodeCount());
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
