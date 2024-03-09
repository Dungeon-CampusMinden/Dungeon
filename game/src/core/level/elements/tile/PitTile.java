package core.level.elements.tile;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;

/**
 * Represents an empty void in the dungeon, where the player can fall into. A Pit can be open or
 * closed. If it is open, the player can fall into it. If it is closed, the player can walk over it.
 */
public class PitTile extends Tile {

  private boolean open;
  private long timeToOpen;

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
    this.open = true;
    this.timeToOpen = 0;
  }

  @Override
  public boolean isAccessible() {
    return !open; // Prevent NPCs from walking over open pits
  }

  /** Open the pit. */
  public void open() {
    open = true;
  }

  /** Close the pit. */
  public void close() {
    open = false;
  }

  /**
   * Check if the pit is open.
   *
   * @return true if the pit is open, false if it is closed.
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Sets the time it takes for the pit to open after an entity steps on it.
   *
   * @param time The time in milliseconds it takes for the pit to open. Must be a positive value.
   * @throws IllegalArgumentException if the provided time is negative.
   */
  public void timeToOpen(long time) {
    if (time < 0) {
      throw new IllegalArgumentException("Time to open must be positive.");
    }
    timeToOpen = time;
  }

  /**
   * Gets the time it takes for the pit to open after an entity steps on it.
   *
   * @return The time in milliseconds it takes for the pit to open.
   */
  public long timeToOpen() {
    return timeToOpen;
  }
}
