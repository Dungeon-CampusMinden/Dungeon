package core.level.elements.tile;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

/** Represents an empty void in the dungeon, where the player can fall into. */
public class PitTile extends Tile {

  /**
   * Creates a new Pit.
   *
   * @param texturePath Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public PitTile(
      final IPath texturePath, final Coordinate globalPosition, final DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
    levelElement = LevelElement.PIT;
  }
}
