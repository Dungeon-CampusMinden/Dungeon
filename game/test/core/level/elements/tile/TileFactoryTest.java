package core.level.elements.tile;

import static org.junit.Assert.*;

import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.position.Point;

import org.junit.Test;

public class TileFactoryTest {

    /** checks if Tile of type SKIP can be generated.... */
    @Test
    public void createSKIPTile() {
        Tile t =
                TileFactory.createTile("", new Point(0, 0), LevelElement.SKIP, DesignLabel.DEFAULT);
        assertEquals(SkipTile.class, t.getClass());
        assertEquals(0, t.position().point().x_i());
        assertEquals(0, t.position().point().y_i());
        assertEquals(LevelElement.SKIP, t.levelElement());
        assertEquals(DesignLabel.DEFAULT, t.designLabel());
        assertNull("No Level should be set for a newly created Tile", t.level());
    }
    /** checks if Tile of type FLOOR can be generated.... */
    @Test
    public void createFLOORTile() {
        Tile t =
                TileFactory.createTile(
                        "", new Point(0, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
        assertEquals(FloorTile.class, t.getClass());
        assertEquals(0, t.position().point().x_i());
        assertEquals(0, t.position().point().y_i());
        assertEquals(LevelElement.FLOOR, t.levelElement());
        assertEquals(DesignLabel.DEFAULT, t.designLabel());
        assertNull("No Level should be set for a newly created Tile", t.level());
    }
    /** checks if Tile of type WALL can be generated.... */
    @Test
    public void createWALLTile() {
        Tile t =
                TileFactory.createTile("", new Point(0, 0), LevelElement.WALL, DesignLabel.DEFAULT);
        assertEquals(WallTile.class, t.getClass());
        assertEquals(0, t.position().point().x_i());
        assertEquals(0, t.position().point().y_i());
        assertEquals(LevelElement.WALL, t.levelElement());
        assertEquals(DesignLabel.DEFAULT, t.designLabel());
        assertNull("No Level should be set for a newly created Tile", t.level());
    }
    /** checks if Tile of type HOLE can be generated.... */
    @Test
    public void createHOLETile() {
        Tile t =
                TileFactory.createTile("", new Point(0, 0), LevelElement.HOLE, DesignLabel.DEFAULT);
        assertEquals(HoleTile.class, t.getClass());
        assertEquals(0, t.position().point().x_i());
        assertEquals(0, t.position().point().y_i());
        assertEquals(LevelElement.HOLE, t.levelElement());
        assertEquals(DesignLabel.DEFAULT, t.designLabel());
        assertNull("No Level should be set for a newly created Tile", t.level());
    }
    /** checks if Tile of type EXIT can be generated.... */
    @Test
    public void createEXITTile() {
        Tile t =
                TileFactory.createTile("", new Point(0, 0), LevelElement.EXIT, DesignLabel.DEFAULT);
        assertEquals(ExitTile.class, t.getClass());
        assertEquals(0, t.position().point().x_i());
        assertEquals(0, t.position().point().y_i());
        assertEquals(LevelElement.EXIT, t.levelElement());
        assertEquals(DesignLabel.DEFAULT, t.designLabel());
        assertNull("No Level should be set for a newly created Tile", t.level());
    }
    /** checks if Tile of type DOOR can be generated.... */
    @Test
    public void createDOORTile() {
        Tile t =
                TileFactory.createTile("", new Point(0, 0), LevelElement.DOOR, DesignLabel.DEFAULT);
        assertEquals(DoorTile.class, t.getClass());
        assertEquals(0, t.position().point().x_i());
        assertEquals(0, t.position().point().y_i());
        assertEquals(LevelElement.DOOR, t.levelElement());
        assertEquals(DesignLabel.DEFAULT, t.designLabel());
        assertNull("No Level should be set for a newly created Tile", t.level());
    }
}
