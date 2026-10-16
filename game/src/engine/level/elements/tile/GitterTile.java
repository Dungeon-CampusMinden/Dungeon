package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;

/** Represents a Gitter in the game. */
public class GitterTile extends Tile {
  /**
   * Creates a new Tile.
   *
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public GitterTile(Coordinate globalPosition, DesignLabel designLabel) {
    super(globalPosition, designLabel);
    this.levelElement = LevelElement.GITTER;
  }
}
