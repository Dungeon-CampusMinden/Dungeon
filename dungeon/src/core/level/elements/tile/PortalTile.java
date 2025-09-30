package core.level.elements.tile;

<<<<<<< HEAD
<<<<<<< HEAD
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

public class PortalTile extends Tile {
=======
=======
import core.level.Tile;
>>>>>>> 40c748e7 (added PortalTile for portal skill)
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

<<<<<<< HEAD
public class PortalTile extends WallTile {
>>>>>>> ca4f8ed2 (added dummy portal skill)
=======
public class PortalTile extends Tile {
>>>>>>> 40c748e7 (added PortalTile for portal skill)
  /**
   * Creates a new Tile.
   *
   * @param texturePath    Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel    Design of the Tile
   */
<<<<<<< HEAD
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel, LevelElement levelElement) {
    super(texturePath, globalPosition, designLabel);
    this.levelElement = levelElement;
=======
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
<<<<<<< HEAD
>>>>>>> ca4f8ed2 (added dummy portal skill)
=======
    levelElement = LevelElement.PORTAL;
>>>>>>> 40c748e7 (added PortalTile for portal skill)
  }
}
