package level.elements.tile;

import static org.junit.Assert.*;

import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Test;

public class TileFactoryTest {

    /** checks if Tile of type SKIP can be generated.... */
    @Test
    public void createSKIPTile() {
        Tile t =
                TileFactory.createTile(
                        "", new Coordinate(0, 0), LevelElement.SKIP, DesignLabel.DEFAULT);
        assertEquals(SkipTile.class, t.getClass());
        assertEquals(0, t.getCoordinate().x);
        assertEquals(0, t.getCoordinate().y);
        assertEquals(LevelElement.SKIP, t.getLevelElement());
        assertEquals(DesignLabel.DEFAULT, t.getDesignLabel());
        assertNull("No Level should be set for a newly created Tile", t.getLevel());
    }
    /** checks if Tile of type FLOOR can be generated.... */
    @Test
    public void createFLOORTile() {
        Tile t =
                TileFactory.createTile(
                        "", new Coordinate(0, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
        assertEquals(FloorTile.class, t.getClass());
        assertEquals(0, t.getCoordinate().x);
        assertEquals(0, t.getCoordinate().y);
        assertEquals(LevelElement.FLOOR, t.getLevelElement());
        assertEquals(DesignLabel.DEFAULT, t.getDesignLabel());
        assertNull("No Level should be set for a newly created Tile", t.getLevel());
    }
    /** checks if Tile of type WALL can be generated.... */
    @Test
    public void createWALLTile() {
        Tile t =
                TileFactory.createTile(
                        "", new Coordinate(0, 0), LevelElement.WALL, DesignLabel.DEFAULT);
        assertEquals(WallTile.class, t.getClass());
        assertEquals(0, t.getCoordinate().x);
        assertEquals(0, t.getCoordinate().y);
        assertEquals(LevelElement.WALL, t.getLevelElement());
        assertEquals(DesignLabel.DEFAULT, t.getDesignLabel());
        assertNull("No Level should be set for a newly created Tile", t.getLevel());
    }
    /** checks if Tile of type HOLE can be generated.... */
    @Test
    public void createHOLETile() {
        Tile t =
                TileFactory.createTile(
                        "", new Coordinate(0, 0), LevelElement.HOLE, DesignLabel.DEFAULT);
        assertEquals(HoleTile.class, t.getClass());
        assertEquals(0, t.getCoordinate().x);
        assertEquals(0, t.getCoordinate().y);
        assertEquals(LevelElement.HOLE, t.getLevelElement());
        assertEquals(DesignLabel.DEFAULT, t.getDesignLabel());
        assertNull("No Level should be set for a newly created Tile", t.getLevel());
    }
    /** checks if Tile of type EXIT can be generated.... */
    @Test
    public void createEXITTile() {
        Tile t =
                TileFactory.createTile(
                        "", new Coordinate(0, 0), LevelElement.EXIT, DesignLabel.DEFAULT);
        assertEquals(ExitTile.class, t.getClass());
        assertEquals(0, t.getCoordinate().x);
        assertEquals(0, t.getCoordinate().y);
        assertEquals(LevelElement.EXIT, t.getLevelElement());
        assertEquals(DesignLabel.DEFAULT, t.getDesignLabel());
        assertNull("No Level should be set for a newly created Tile", t.getLevel());
    }
    /** checks if Tile of type DOOR can be generated.... */
    @Test
    public void createDOORTile() {
        Tile t =
                TileFactory.createTile(
                        "", new Coordinate(0, 0), LevelElement.DOOR, DesignLabel.DEFAULT);
        assertEquals(DoorTile.class, t.getClass());
        assertEquals(0, t.getCoordinate().x);
        assertEquals(0, t.getCoordinate().y);
        assertEquals(LevelElement.DOOR, t.getLevelElement());
        assertEquals(DesignLabel.DEFAULT, t.getDesignLabel());
        assertNull("No Level should be set for a newly created Tile", t.getLevel());
    }
}
