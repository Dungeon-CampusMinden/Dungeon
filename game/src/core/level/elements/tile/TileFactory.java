package core.level.elements.tile;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

/** Factory to create a specific {@link Tile} based on the given element type. */
public class TileFactory {

  /**
   * Creates a new Tile which can then be added to the level.
   *
   * @param texturePath the path to the texture
   * @param coordinate the position of the newly created Tile
   * @param elementType the type of the new Tile
   * @param designLabel the label for reasons
   * @return the newly created Tile
   */
  public static Tile createTile(
      final IPath texturePath,
      final Coordinate coordinate,
      final LevelElement elementType,
      final DesignLabel designLabel) {
    return switch (elementType) {
      case FLOOR -> new FloorTile(texturePath, coordinate, designLabel);
      case WALL -> new WallTile(texturePath, coordinate, designLabel);
      case HOLE -> new HoleTile(texturePath, coordinate, designLabel);
      case DOOR -> new DoorTile(texturePath, coordinate, designLabel);
      case EXIT -> new ExitTile(texturePath, coordinate, designLabel);
      case PIT -> new PitTile(texturePath, coordinate, designLabel);
      case SKIP -> new SkipTile(texturePath, coordinate, designLabel);
    };
  }
}
