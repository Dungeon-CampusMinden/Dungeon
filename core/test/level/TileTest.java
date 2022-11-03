package level;

import static org.junit.Assert.*;

import level.elements.Tile;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Test;

public class TileTest {

    @Test
    public void test_isAccessible() {
        Coordinate dummyCoordinate = new Coordinate(0, 0);
        Tile wall = new Tile("", dummyCoordinate, LevelElement.WALL, DesignLabel.DEFAULT);
        Tile floor = new Tile("", dummyCoordinate, LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile exit = new Tile("", dummyCoordinate, LevelElement.EXIT, DesignLabel.DEFAULT);
        Tile start = new Tile("", dummyCoordinate, LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile skip = new Tile("", dummyCoordinate, LevelElement.SKIP, DesignLabel.DEFAULT);
        assertTrue(floor.isAccessible());
        assertTrue(exit.isAccessible());
        assertTrue(start.isAccessible());
        assertFalse(wall.isAccessible());
        assertFalse(skip.isAccessible());
    }

    @Test
    public void test_directionTo() {
        Tile north = new Tile("", new Coordinate(0, 1), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile south = new Tile("", new Coordinate(0, -1), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile east = new Tile("", new Coordinate(1, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile west = new Tile("", new Coordinate(-1, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile northEast =
                new Tile("", new Coordinate(1, 1), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile northWest =
                new Tile("", new Coordinate(-1, 1), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile southEast =
                new Tile("", new Coordinate(1, -1), LevelElement.FLOOR, DesignLabel.DEFAULT);
        Tile southWest =
                new Tile("", new Coordinate(-1, -1), LevelElement.FLOOR, DesignLabel.DEFAULT);

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
