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
 *
 * <p>You need to configure the door with {@link #otherDoor(DoorTile)} and {@link #doorstep(Tile)}.
 */
public class DoorTile extends Tile {

  private final IPath closedTexturePath;
  private DoorTile otherDoor;
  private Tile doorstep;
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
    String[] splitPath = texturePath.pathString().split("\\.");
    closedTexturePath = new SimpleIPath(splitPath[0] + "_closed." + splitPath[1]);
    levelElement = LevelElement.DOOR;
    open = true;
  }

  @Override
  public boolean isAccessible() {
    if (!open || (otherDoor != null && !otherDoor.isOpen())) return false;
    else return levelElement.value();
  }

  @Override
  public boolean canSeeThrough() {
    return open;
  }

  /**
   * Connects this door with its other side in another room.
   *
   * @param otherDoor Door that will be connected to this door
   */
  public void otherDoor(DoorTile otherDoor) {
    this.otherDoor = otherDoor;
  }

  /**
   * @return Door that is connected to this door
   */
  public DoorTile otherDoor() {
    return otherDoor;
  }

  /**
   * Sets Tile in front of the door.
   *
   * @param doorstep Tile in front of the door
   */
  public void doorstep(final Tile doorstep) {
    this.doorstep = doorstep;
  }

  /**
   * Get Tile in front ot the door.
   *
   * @return Tile in front of the door.
   */
  public Tile doorstep() {
    return doorstep;
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
    if (open && (otherDoor == null || otherDoor.isOpen())) return texturePath;
    else return closedTexturePath;
  }

  @Override
  public String toString() {
    String tileStr = super.toString();
    tileStr = tileStr.replace("Tile", "DoorTile").replace("}", "");
    String doorStepStr = this.doorstep == null ? "null" : this.doorstep.coordinate().toString();
    String otherDoorStr = this.otherDoor == null ? "null" : this.otherDoor.coordinate().toString();
    return tileStr
        + ", closedTexturePath="
        + this.closedTexturePath.pathString()
        + ", open="
        + this.open
        + ", Doorstep="
        + doorStepStr
        + ", OtherDoor="
        + otherDoorStr
        + "}";
  }
}
