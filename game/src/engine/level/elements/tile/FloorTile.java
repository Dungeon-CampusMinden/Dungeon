package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;

/** Represents a Floor in the dungeon. */
public class FloorTile extends Tile {

  /**
   * Creates a new Tile.
   *
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public FloorTile(final Coordinate globalPosition, final DesignLabel designLabel) {
    super(globalPosition, designLabel);
    levelElement = LevelElement.FLOOR;
  }
}
