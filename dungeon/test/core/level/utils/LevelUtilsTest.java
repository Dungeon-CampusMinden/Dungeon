package core.level.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Game;
import core.level.DungeonLevel;
import core.systems.LevelSystem;
import core.utils.IVoidFunction;
import core.utils.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
  @BeforeEach
  public void setup() {
    Game.add(new LevelSystem(Mockito.mock(IVoidFunction.class)));

    Game.currentLevel(
        new DungeonLevel(
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
            DesignLabel.DEFAULT));
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllSystems();
  }

  /** WTF? . */
  @Test
  public void tilesInRangeCenterNotInLevel() {
    var tiles = LevelUtils.tilesInRange(new Point(-10, -10), 1.1f);
    assertEquals(0, tiles.size());
  }

  /** WTF? . */
  @Test
  public void tilesInRangeOnlyCorners() {
    var tiles = LevelUtils.tilesInRange(new Point(0, 0), 1.1f);
    assertEquals(3, tiles.size());
    assertTrue(
        tiles.stream().anyMatch(tile -> tile.coordinate().x() == 0 && tile.coordinate().y() == 0));
    assertTrue(
        tiles.stream().anyMatch(tile -> tile.coordinate().x() == 1 && tile.coordinate().y() == 0));
    assertTrue(
        tiles.stream().anyMatch(tile -> tile.coordinate().x() == 0 && tile.coordinate().y() == 1));
  }

  /** WTF? . */
  @Test
  public void tilesInRangeNotInCorner() {
    var tiles = LevelUtils.tilesInRange(new Point(0.5f, 0.5f), 0.6f);
    assertEquals(3, tiles.size());
    assertTrue(
        tiles.stream().anyMatch(tile -> tile.coordinate().x() == 0 && tile.coordinate().y() == 0));
    assertTrue(
        tiles.stream().anyMatch(tile -> tile.coordinate().x() == 1 && tile.coordinate().y() == 0));
    assertTrue(
        tiles.stream().anyMatch(tile -> tile.coordinate().x() == 0 && tile.coordinate().y() == 1));
  }
}
