package level;

import static org.junit.Assert.*;

import level.elements.tile.*;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import org.junit.Test;

public class TileTest {

    @Test
    public void test_isAccessible() {
        Coordinate dummyCoordinate = new Coordinate(0, 0);
        Tile wall = new WallTile("", dummyCoordinate, DesignLabel.DEFAULT, null);
        Tile floor = new FloorTile("", dummyCoordinate, DesignLabel.DEFAULT, null);
        Tile exit = new ExitTile("", dummyCoordinate, DesignLabel.DEFAULT, null);
        Tile start = new FloorTile("", dummyCoordinate, DesignLabel.DEFAULT, null);
        Tile skip = new SkipTile("", dummyCoordinate, DesignLabel.DEFAULT, null);
        assertTrue(floor.isAccessible());
        assertTrue(exit.isAccessible());
        assertTrue(start.isAccessible());
        assertFalse(wall.isAccessible());
        assertFalse(skip.isAccessible());
    }

    @Test
    public void test_directionTo() {
        Tile north = new FloorTile("", new Coordinate(0, 1), DesignLabel.DEFAULT, null);
        Tile south = new FloorTile("", new Coordinate(0, -1), DesignLabel.DEFAULT, null);
        Tile east = new FloorTile("", new Coordinate(1, 0), DesignLabel.DEFAULT, null);
        Tile west = new FloorTile("", new Coordinate(-1, 0), DesignLabel.DEFAULT, null);
        Tile northEast = new FloorTile("", new Coordinate(1, 1), DesignLabel.DEFAULT, null);
        Tile northWest = new FloorTile("", new Coordinate(-1, 1), DesignLabel.DEFAULT, null);
        Tile southEast = new FloorTile("", new Coordinate(1, -1), DesignLabel.DEFAULT, null);
        Tile southWest = new FloorTile("", new Coordinate(-1, -1), DesignLabel.DEFAULT, null);

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
