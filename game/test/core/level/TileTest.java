package core.level;

import static org.junit.Assert.*;

import core.level.elements.tile.ExitTile;
import core.level.elements.tile.FloorTile;
import core.level.elements.tile.SkipTile;
import core.level.elements.tile.WallTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.utils.components.path.SimpleIPath;
import org.junit.Test;

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

    Tile.Direction[] northToSouth = north.directionTo(south);
    assertEquals(1, northToSouth.length);
    assertEquals(Tile.Direction.S, northToSouth[0]);

    Tile.Direction[] southToNorth = south.directionTo(north);
    assertEquals(1, southToNorth.length);
    assertEquals(Tile.Direction.N, southToNorth[0]);

    Tile.Direction[] eastToWest = east.directionTo(west);
    assertEquals(1, eastToWest.length);
    assertEquals(Tile.Direction.W, eastToWest[0]);

    Tile.Direction[] westToEast = west.directionTo(east);
    assertEquals(1, westToEast.length);
    assertEquals(Tile.Direction.E, westToEast[0]);

    Tile.Direction[] southEastToNorthWest = southEast.directionTo(northWest);
    assertEquals(2, southEastToNorthWest.length);
    assertEquals(Tile.Direction.W, southEastToNorthWest[0]);
    assertEquals(Tile.Direction.N, southEastToNorthWest[1]);

    Tile.Direction[] southWestToNorthEast = southWest.directionTo(northEast);
    assertEquals(2, southWestToNorthEast.length);
    assertEquals(Tile.Direction.E, southWestToNorthEast[0]);
    assertEquals(Tile.Direction.N, southWestToNorthEast[1]);

    Tile.Direction[] northEastToSouthWest = northEast.directionTo(southWest);
    assertEquals(2, northEastToSouthWest.length);
    assertEquals(Tile.Direction.W, northEastToSouthWest[0]);
    assertEquals(Tile.Direction.S, northEastToSouthWest[1]);

    Tile.Direction[] northWestToSouthEast = northWest.directionTo(southEast);
    assertEquals(2, northWestToSouthEast.length);
    assertEquals(Tile.Direction.E, northWestToSouthEast[0]);
    assertEquals(Tile.Direction.S, northWestToSouthEast[1]);
  }
}
