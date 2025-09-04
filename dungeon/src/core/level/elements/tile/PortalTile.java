package core.level.elements.tile;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.utils.components.path.IPath;

public class PortalTile extends WallTile {
  /**
   * Creates a new Tile.
   *
   * @param texturePath    Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel    Design of the Tile
   */
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
  }
}
