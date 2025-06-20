package core.level;

import static org.junit.jupiter.api.Assertions.*;

import core.level.elements.tile.ExitTile;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.SkipTile;
import core.level.elements.tile.WallTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.utils.Direction;
import core.utils.components.path.SimpleIPath;
import org.junit.jupiter.api.Test;

/** Tests for the {@link Tile} class. */
public class TileTest {
  /** WTF? . */
  @Test
  public void test_isAccessible() {
    Coordinate dummyCoordinate = new Coordinate(0, 0);
    Tile wall = new WallTile(new SimpleIPath(""), dummyCoordinate, DesignLabel.DEFAULT);
    Tile floor = new FloorTile(new SimpleIPath(""), dummyCoordinate, DesignLabel.DEFAULT);
    Tile exit = new ExitTile(new SimpleIPath(""), dummyCoordinate, DesignLabel.DEFAULT);
    Tile start = new FloorTile(new SimpleIPath(""), dummyCoordinate, DesignLabel.DEFAULT);
    Tile skip = new SkipTile(new SimpleIPath(""), dummyCoordinate, DesignLabel.DEFAULT);
    assertTrue(floor.isAccessible());
    assertTrue(exit.isAccessible());
    assertTrue(start.isAccessible());
    assertFalse(wall.isAccessible());
    assertFalse(skip.isAccessible());
  }

  /** WTF? . */
  @Test
  public void test_directionTo() {
    Tile north = new FloorTile(new SimpleIPath(""), new Coordinate(0, 1), DesignLabel.DEFAULT);
    Tile south = new FloorTile(new SimpleIPath(""), new Coordinate(0, -1), DesignLabel.DEFAULT);
    Tile east = new FloorTile(new SimpleIPath(""), new Coordinate(1, 0), DesignLabel.DEFAULT);
    Tile west = new FloorTile(new SimpleIPath(""), new Coordinate(-1, 0), DesignLabel.DEFAULT);
    Tile northEast = new FloorTile(new SimpleIPath(""), new Coordinate(1, 1), DesignLabel.DEFAULT);
    Tile northWest = new FloorTile(new SimpleIPath(""), new Coordinate(-1, 1), DesignLabel.DEFAULT);
    Tile southEast = new FloorTile(new SimpleIPath(""), new Coordinate(1, -1), DesignLabel.DEFAULT);
    Tile southWest =
        new FloorTile(new SimpleIPath(""), new Coordinate(-1, -1), DesignLabel.DEFAULT);

    Direction[] northToSouth = north.directionTo(south);
    assertEquals(1, northToSouth.length);
    assertEquals(Direction.DOWN, northToSouth[0]);

    Direction[] southToNorth = south.directionTo(north);
    assertEquals(1, southToNorth.length);
    assertEquals(Direction.UP, southToNorth[0]);

    Direction[] eastToWest = east.directionTo(west);
    assertEquals(1, eastToWest.length);
    assertEquals(Direction.LEFT, eastToWest[0]);

    Direction[] westToEast = west.directionTo(east);
    assertEquals(1, westToEast.length);
    assertEquals(Direction.RIGHT, westToEast[0]);

    Direction[] southEastToNorthWest = southEast.directionTo(northWest);
    assertEquals(2, southEastToNorthWest.length);
    assertEquals(Direction.LEFT, southEastToNorthWest[0]);
    assertEquals(Direction.UP, southEastToNorthWest[1]);

    Direction[] southWestToNorthEast = southWest.directionTo(northEast);
    assertEquals(2, southWestToNorthEast.length);
    assertEquals(Direction.RIGHT, southWestToNorthEast[0]);
    assertEquals(Direction.UP, southWestToNorthEast[1]);

    Direction[] northEastToSouthWest = northEast.directionTo(southWest);
    assertEquals(2, northEastToSouthWest.length);
    assertEquals(Direction.LEFT, northEastToSouthWest[0]);
    assertEquals(Direction.DOWN, northEastToSouthWest[1]);

    Direction[] northWestToSouthEast = northWest.directionTo(southEast);
    assertEquals(2, northWestToSouthEast.length);
    assertEquals(Direction.RIGHT, northWestToSouthEast[0]);
    assertEquals(Direction.DOWN, northWestToSouthEast[1]);
  }
}
