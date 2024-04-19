package core.level.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import core.Game;
import core.level.TileLevel;
import core.level.generator.IGenerator;
import core.systems.LevelSystem;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.draw.Painter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for the {@link LevelUtils} class. */
public class LevelUtilsTest {

  // default layout is:
  //
  // W W W W W
  // W F F F W
  // W F E F W
  // W F F F W
  // W W W W W
  /** WTF? . */
  @Before
  public void setup() {
    Game.add(
        new LevelSystem(
            Mockito.mock(Painter.class),
            Mockito.mock(IGenerator.class),
            Mockito.mock(IVoidFunction.class)));

    Game.currentLevel(
        new TileLevel(
            new LevelElement[][] {
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.FLOOR,
                LevelElement.EXIT,
                LevelElement.FLOOR,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
              }
            },
            DesignLabel.randomDesign()));
  }

  /** WTF? . */
  @After
  public void cleanup() {
    Game.removeAllSystems();
  }

  /** WTF? . */
  @Test
  public void tilesInRangeCenterNotInLevel() {
    var tiles = LevelUtils.tilesInRange(new Point(-10, -10), 1.1f);
    assertEquals("tile is outside of the level no Tile should be returned", 0, tiles.size());
  }

  /** WTF? . */
  @Test
  public void tilesInRangeOnlyCorners() {
    var tiles = LevelUtils.tilesInRange(new Point(0, 0), 1.1f);
    assertEquals("should have 3", 3, tiles.size());
    assertTrue(
        "should contain tile at 0,0 which is the requestPosition",
        tiles.stream().anyMatch(tile -> tile.coordinate().x == 0 && tile.coordinate().y == 0));
    assertTrue(
        "should contain tile at 1,0 which is in range",
        tiles.stream().anyMatch(tile -> tile.coordinate().x == 1 && tile.coordinate().y == 0));
    assertTrue(
        "should contain tile at 0,1 which is in range",
        tiles.stream().anyMatch(tile -> tile.coordinate().x == 0 && tile.coordinate().y == 1));
  }

  /** WTF? . */
  @Test
  public void tilesInRangeNotInCorner() {
    var tiles = LevelUtils.tilesInRange(new Point(0.5f, 0.5f), 0.6f);
    assertEquals("should have 3", 3, tiles.size());
    assertTrue(
        "should contain tile at 0,0 which is the requestPosition",
        tiles.stream().anyMatch(tile -> tile.coordinate().x == 0 && tile.coordinate().y == 0));
    assertTrue(
        "should contain tile at 1,0 which is in range",
        tiles.stream().anyMatch(tile -> tile.coordinate().x == 1 && tile.coordinate().y == 0));
    assertTrue(
        "should contain tile at 0,1 which is in range",
        tiles.stream().anyMatch(tile -> tile.coordinate().x == 0 && tile.coordinate().y == 1));
  }
}
