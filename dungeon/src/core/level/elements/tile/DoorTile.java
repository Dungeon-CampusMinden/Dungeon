package core.level.elements.tile;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Represents a Door in the game.
 *
 * <p>A Door connects two room-based levels.
 */
public class DoorTile extends Tile {

  private boolean open;

  /**
   * Creates a new Tile.
   *
   * <p>The door will be open.
   *
   * @param texturePath Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public DoorTile(
      final IPath texturePath, final Coordinate globalPosition, final DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);
    levelElement = LevelElement.DOOR;
    open = true;
  }

  @Override
  public boolean isAccessible() {
    return isOpen();
  }

  @Override
  public boolean canSeeThrough() {
    return this.open;
  }

  /**
   * Open the door.
   *
   * <p>The player can use the door to enter the next room.
   */
  public void open() {
    open = true;
  }

  /**
   * Close the door.
   *
   * <p>The player can't use the door to enter the next room.
   */
  public void close() {
    open = false;
  }

  /**
   * Check if the door is open.
   *
   * @return true if the door is open, false if not.
   */
  public boolean isOpen() {
    return open;
  }

  @Override
  public IPath texturePath() {
    if (open) return texturePath;
    else return closedTexturePath();
  }

  @Override
  public String toString() {
    String tileStr = super.toString();
    tileStr = tileStr.replace("Tile", "DoorTile").replace("}", "");
    String closedTexturePathStr =
        closedTexturePath() == null ? "null" : closedTexturePath().pathString();
    return tileStr + ", closedTexturePath=" + closedTexturePathStr + ", open=" + this.open + "}";
  }

  private IPath closedTexturePath() {
    if (texturePath != null) {
      String[] splitPath = texturePath.pathString().split("\\.");
      return new SimpleIPath(splitPath[0] + "_closed." + splitPath[1]);
    } else {
      return null;
    }
  }
}
