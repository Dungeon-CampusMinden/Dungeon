package engine.level.elements.tile;

import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;

/** Represents a Portal in the game. */
public class PortalTile extends Tile {
  /**
   * Creates a new Tile.
   *
   * @param globalPosition Position of the tile in the global system.
   * @param designLabel Design of the Tile
   */
  public PortalTile(Coordinate globalPosition, DesignLabel designLabel) {
    super(globalPosition, designLabel);
    this.levelElement = LevelElement.PORTAL;
  }
}
