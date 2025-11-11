package core.utils.components.draw;

import core.level.Tile;
import core.level.elements.tile.PitTile;

/** Utility class for Tile-related operations. */
public class TileUtil {

  /**
   * Checks if the provided tile is an instance of PitTile and if it's open.
   *
   * @param tile The tile to check.
   * @return true if the tile is an instance of PitTile, and it's open, false otherwise.
   */
  public static boolean isTilePitAndOpen(final Tile tile) {
    if (tile instanceof PitTile) {
      return ((PitTile) tile).isOpen();
    } else {
      return false;
    }
  }
}
