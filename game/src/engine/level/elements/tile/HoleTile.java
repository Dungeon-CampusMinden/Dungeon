package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.components.path.IPath;

/** Represents a hole tile in the dungeon. */
public class HoleTile extends Tile {

  /**
   * Creates a new Tile.
   *
   * @param texturePath Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public HoleTile(
      final IPath texturePath, final Coordinate globalPosition, final DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
    levelElement = LevelElement.HOLE;
  }
}
