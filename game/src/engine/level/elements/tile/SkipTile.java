package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.components.path.IPath;

/** Represents an empty void in the dungeon. */
public class SkipTile extends Tile {

  /**
   * Creates a new Tile.
   *
   * @param texturePath Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public SkipTile(
      final IPath texturePath, final Coordinate globalPosition, final DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
    levelElement = LevelElement.SKIP;
  }
}
