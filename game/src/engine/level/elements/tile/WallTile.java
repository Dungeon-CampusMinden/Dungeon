package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;

/** Represents a Wall in the game. */
public class WallTile extends Tile {

  /**
   * Creates a new Tile.
   *
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public WallTile(final Coordinate globalPosition, final DesignLabel designLabel) {
    super(globalPosition, designLabel);
    levelElement = LevelElement.WALL;
  }
}
