package engine.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
import engine.level.elements.tile.FloorTile;
import engine.level.elements.tile.SkipTile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import org.junit.jupiter.api.Test;

/** Tests for the occlusion map of the {@link AtmosphereRenderer}. */
public class AtmosphereRendererTest {

  /** Closed doors block light without casting ambient occlusion, the void the other way round. */
  @Test
  public void occlusionTexelSeparatesLightBlockersFromCasters() {
    DoorTile closedDoor = new DoorTile(new Coordinate(1, 0), DesignLabel.DEFAULT);
    closedDoor.close();
    Tile[][] layout = {
      {
        new FloorTile(new Coordinate(0, 0), DesignLabel.DEFAULT),
        closedDoor,
        new SkipTile(new Coordinate(2, 0), DesignLabel.DEFAULT)
      }
    };

    assertEquals(0x00ffff00, AtmosphereRenderer.occlusionTexel(layout, 0, 0), "floor");
    assertEquals(0xaa000000, AtmosphereRenderer.occlusionTexel(layout, 1, 0), "closed door");
    assertEquals(0x00000000, AtmosphereRenderer.occlusionTexel(layout, 2, 0), "void");
    assertEquals(0xff000000, AtmosphereRenderer.occlusionTexel(layout, 3, 0), "outside");
  }
}
