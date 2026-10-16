package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;

/**
 * Represents a Door in the game.
 *
 * <p>A Door connects two room-based levels.
 */
public class DoorTile extends Tile {

  private boolean open;
  private boolean hidden;

  /**
   * Creates a new Tile.
   *
   * <p>The door will be open.
   *
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public DoorTile(final Coordinate globalPosition, final DesignLabel designLabel) {
    super(globalPosition, designLabel);
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

  /**
   * Whether this is a hidden door: it looks like the surrounding wall while closed and like floor
   * while open, like a gate that opens a passage.
   *
   * @return true if the door is hidden
   */
  public boolean hidden() {
    return hidden;
  }

  /**
   * Makes this a hidden door, see {@link #hidden()}.
   *
   * @param hidden whether the door is hidden
   */
  public void hidden(boolean hidden) {
    this.hidden = hidden;
  }

  @Override
  public String toString() {
    String tileStr = super.toString();
    tileStr = tileStr.replace("Tile", "DoorTile").replace("}", "");
    return tileStr + ", open=" + this.open + "}";
  }
}
