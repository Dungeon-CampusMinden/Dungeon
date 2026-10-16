package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;

/** Factory to create a specific {@link Tile} based on the given element type. */
public class TileFactory {

  /**
   * Creates a new Tile which can then be added to the level.
   *
   * @param coordinate the position of the newly created Tile
   * @param elementType the type of the new Tile
   * @param designLabel the label for reasons
   * @return the newly created Tile
   */
  public static Tile createTile(
      final Coordinate coordinate, final LevelElement elementType, final DesignLabel designLabel) {
    return switch (elementType) {
      case FLOOR -> new FloorTile(coordinate, designLabel);
      case WALL -> new WallTile(coordinate, designLabel);
      case HOLE -> new HoleTile(coordinate, designLabel);
      case DOOR -> new DoorTile(coordinate, designLabel);
      case EXIT -> new ExitTile(coordinate, designLabel);
      case SKIP -> new SkipTile(coordinate, designLabel);
      case PIT -> new PitTile(coordinate, designLabel);
      case PORTAL -> new PortalTile(coordinate, designLabel);
      case GITTER -> new GitterTile(coordinate, designLabel);
      case GLASSWALL -> new GlasswandTile(coordinate, designLabel);
    };
  }
}
