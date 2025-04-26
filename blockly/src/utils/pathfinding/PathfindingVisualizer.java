package utils.pathfinding;

import core.Game;
import core.level.Tile;
import core.utils.Tuple;

/**
 * This class is responsible for visualizing the pathfinding process by coloring tiles based on
 * their state.
 */
public class PathfindingVisualizer {

  /**
   * Color a tile based on its state.
   *
   * <p>This method takes a tuple containing a node and its corresponding tile state, and colors the
   * tile accordingly.
   *
   * @param blockToColor A tuple containing the node and its tile state.
   * @see Tuple
   */
  public static void colorTile(Tuple<Node, TileState> blockToColor) {
    Tile tile = Game.tileAT(blockToColor.a().coordinate());
    if (tile == null) {
      return;
    }

    tile.tintColor(blockToColor.b().color());
  }
}
