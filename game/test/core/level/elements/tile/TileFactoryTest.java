package core.level.elements.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import org.junit.Test;

/** Tests for {@link TileFactory} class. */
public class TileFactoryTest {

  /** Checks if Tile of type SKIP can be generated. */
  @Test
  public void createSKIPTile() {
    Tile t =
        TileFactory.createTile(
            new SimpleIPath(""), new Coordinate(0, 0), LevelElement.SKIP, DesignLabel.DEFAULT);
    assertEquals(SkipTile.class, t.getClass());
    assertEquals(0, t.coordinate().x);
    assertEquals(0, t.coordinate().y);
    assertEquals(LevelElement.SKIP, t.levelElement());
    assertEquals(DesignLabel.DEFAULT, t.designLabel());
    assertNull("No Level should be set for a newly created Tile", t.level());
  }

  /** Checks if Tile of type FLOOR can be generated. */
  @Test
  public void createFLOORTile() {
    Tile t =
        TileFactory.createTile(
            new SimpleIPath(""), new Coordinate(0, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
    assertEquals(FloorTile.class, t.getClass());
    assertEquals(0, t.coordinate().x);
    assertEquals(0, t.coordinate().y);
    assertEquals(LevelElement.FLOOR, t.levelElement());
    assertEquals(DesignLabel.DEFAULT, t.designLabel());
    assertNull("No Level should be set for a newly created Tile", t.level());
  }

  /** Checks if Tile of type WALL can be generated. */
  @Test
  public void createWALLTile() {
    Tile t =
        TileFactory.createTile(
            new SimpleIPath(""), new Coordinate(0, 0), LevelElement.WALL, DesignLabel.DEFAULT);
    assertEquals(WallTile.class, t.getClass());
    assertEquals(0, t.coordinate().x);
    assertEquals(0, t.coordinate().y);
    assertEquals(LevelElement.WALL, t.levelElement());
    assertEquals(DesignLabel.DEFAULT, t.designLabel());
    assertNull("No Level should be set for a newly created Tile", t.level());
  }

  /** Checks if Tile of type HOLE can be generated. */
  @Test
  public void createHOLETile() {
    Tile t =
        TileFactory.createTile(
            new SimpleIPath(""), new Coordinate(0, 0), LevelElement.HOLE, DesignLabel.DEFAULT);
    assertEquals(HoleTile.class, t.getClass());
    assertEquals(0, t.coordinate().x);
    assertEquals(0, t.coordinate().y);
    assertEquals(LevelElement.HOLE, t.levelElement());
    assertEquals(DesignLabel.DEFAULT, t.designLabel());
    assertNull("No Level should be set for a newly created Tile", t.level());
  }

  /** Checks if Tile of type EXIT can be generated. */
  @Test
  public void createEXITTile() {
    Tile t =
        TileFactory.createTile(
            new SimpleIPath(""), new Coordinate(0, 0), LevelElement.EXIT, DesignLabel.DEFAULT);
    assertEquals(ExitTile.class, t.getClass());
    assertEquals(0, t.coordinate().x);
    assertEquals(0, t.coordinate().y);
    assertEquals(LevelElement.EXIT, t.levelElement());
    assertEquals(DesignLabel.DEFAULT, t.designLabel());
    assertNull("No Level should be set for a newly created Tile", t.level());
  }

  /** Checks if Tile of type DOOR can be generated. */
  @Test
  public void createDOORTile() {
    Tile t =
        TileFactory.createTile(
            new SimpleIPath(".png"), new Coordinate(0, 0), LevelElement.DOOR, DesignLabel.DEFAULT);
    assertEquals(DoorTile.class, t.getClass());
    assertEquals(0, t.coordinate().x);
    assertEquals(0, t.coordinate().y);
    assertEquals(LevelElement.DOOR, t.levelElement());
    assertEquals(DesignLabel.DEFAULT, t.designLabel());
    assertNull("No Level should be set for a newly created Tile", t.level());
  }
}
