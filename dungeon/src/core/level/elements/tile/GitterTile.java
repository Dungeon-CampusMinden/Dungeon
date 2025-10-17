package core.level.elements.tile;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

/** Represents a Gitter in the game. */
public class GitterTile extends Tile {
  /**
   * Creates a new Tile.
   *
   * @param texturePath Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public GitterTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
    this.levelElement = LevelElement.GITTER;
  }
}
