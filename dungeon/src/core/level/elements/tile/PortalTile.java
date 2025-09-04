package core.level.elements.tile;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
import core.level.Tile;
>>>>>>> be002c1f (added PortalTile for portal skill)
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

<<<<<<< HEAD
public class PortalTile extends WallTile {
>>>>>>> e27b0a78 (added dummy portal skill)
=======
public class PortalTile extends Tile {
>>>>>>> be002c1f (added PortalTile for portal skill)
=======
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.utils.components.path.IPath;

public class PortalTile extends WallTile {
>>>>>>> 75c6fee5 (added dummy portal skill)
  /**
   * Creates a new Tile.
   *
   * @param texturePath    Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel    Design of the Tile
   */
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel, LevelElement levelElement) {
    super(texturePath, globalPosition, designLabel);
    this.levelElement = levelElement;
=======
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> ca4f8ed2 (added dummy portal skill)
=======
    levelElement = LevelElement.PORTAL;
>>>>>>> 40c748e7 (added PortalTile for portal skill)
=======
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel, LevelElement levelElement) {
    super(texturePath, globalPosition, designLabel);
    this.levelElement = levelElement;
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
>>>>>>> e27b0a78 (added dummy portal skill)
=======
    levelElement = LevelElement.PORTAL;
>>>>>>> be002c1f (added PortalTile for portal skill)
=======
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel, LevelElement levelElement) {
    super(texturePath, globalPosition, designLabel);
    this.levelElement = levelElement;
>>>>>>> 2178e611 (added green and blue portal variants)
=======
  public PortalTile(IPath texturePath, Coordinate globalPosition, DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
>>>>>>> 75c6fee5 (added dummy portal skill)
  }
}
