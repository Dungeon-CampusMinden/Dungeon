package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.components.path.IPath;

/**
 * Represents the exit in the dungeon.
 *
 * <p>If the exit is entered by the player, the {@link engine.systems.LevelSystem} will generate a
 * new level.
 */
public class ExitTile extends Tile {

  private static final int DEFAULT_CLOSE_TINT = 0xFF000066;
  private boolean open = true;

  /**
   * Creates a new Tile.
   *
   * @param texturePath Path to the texture of the tile.
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public ExitTile(
      final IPath texturePath, final Coordinate globalPosition, final DesignLabel designLabel) {
    super(texturePath, globalPosition, designLabel);

    levelElement = LevelElement.EXIT;
  }

  /**
   * Open the exit.
   *
   * <p>The player can now exit the level.
   */
  public void open() {
    this.tintColor(-1); // reset tint
    this.open = true;
  }

  /**
   * Close the exit.
   *
   * <p>The player can no longer exit the level.
   */
  public void close() {
    this.tintColor(DEFAULT_CLOSE_TINT);
    this.open = false;
  }

  @Override
  public boolean isAccessible() {
    return isOpen();
  }

  /**
   * Check if the exit is open.
   *
   * @return true if the exist is open, false if not.
   */
  public boolean isOpen() {
    return this.open;
  }

  @Override
  public String toString() {
    String tileStr = super.toString();
    tileStr = tileStr.replace("Tile", "ExitTile").replace("}", "");
    return tileStr + ", open=" + this.open + "}";
  }
}
