package core.level.elements.tile;

import core.Game;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.astar.TileConnection;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.components.path.IPath;

/**
 * Represents an empty void in the dungeon, where the player can fall into. A Pit can be open or
 * closed. If it is open, the player can fall into it. If it is closed, the player can walk over it.
 */
public class PitTile extends Tile {
  private final IPath stillStableTexturePath;
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
    this.levelElement = LevelElement.PIT;
    this.open = true;
    this.timeToOpen = 0;
    this.stillStableTexturePath =
        TileTextureFactory.findTexturePath(LevelElement.FLOOR, this.designLabel());
  }

  @Override
  public boolean isAccessible() {
    return !this.open;
  }

  /** Open the pit. */
  public void open() {
    this.connections()
        .forEach(
            x ->
                x.getToNode()
                    .connections()
                    .removeValue(new TileConnection(x.getToNode(), this), false));
    this.open = true;
  }

  /** Close the pit. Unless the time to open is set to 0 */
  public void close() {
    if (this.timeToOpen == 0) {
      this.open();
      return;
    }

    Game.currentLevel().addConnectionsToNeighbours(this);
    this.connections.forEach(
        x -> {
          if (!x.getToNode().connections().contains(new TileConnection(x.getToNode(), this), false))
            x.getToNode().addConnection(this);
        });
    this.index(((TileLevel) Game.currentLevel()).nodeCount++);

    this.open = false;
  }

  /**
   * Check if the pit is open.
   *
   * @return true if the pit is open, false if it is closed.
   */
  public boolean isOpen() {
    return this.open;
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
    if (time == 0) {
      this.open();
    }
    this.timeToOpen = time;
  }

  /**
   * Gets the time it takes for the pit to open after an entity steps on it.
   *
   * @return The time in milliseconds it takes for the pit to open.
   */
  public long timeToOpen() {
    return this.timeToOpen;
  }

  @Override
  public IPath texturePath() {
    if (!this.isOpen() && this.timeToOpen > 60 * 1000) return this.stillStableTexturePath;
    return super.texturePath();
  }

  @Override
  public String toString() {
    String tileStr = super.toString();
    tileStr = tileStr.replace("Tile", "PitTile").replace("}", "");
    return tileStr + ", open: " + this.open + ", timeToOpen: " + this.timeToOpen + "}";
  }
}
