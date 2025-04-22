package utils.pathfinding;

import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;

/**
 * This class is responsible for visualizing the pathfinding algorithm's state.
 *
 * <p>It colors the tiles based on their state during the pathfinding process.
 *
 * @see PathfindingState
 */
public class PathfindingVisualizer {

  /**
   * Visualizes the pathfinding algorithm's state by coloring the tiles based on their state.
   *
   * <p>This method colors the tiles in the game world based on their state during the pathfinding
   * process. It uses the {@link TileState} enum to determine the color for each tile.
   *
   * @param state The current state of the pathfinding algorithm.
   */
  public static void drawVisualization(PathfindingState state) {
    for (Coordinate coord : state.openSet()) {
      colorTile(coord, TileState.OPEN);
    }
    for (Coordinate coord : state.closedSet()) {
      colorTile(coord, TileState.CLOSED);
    }
    // Color the current tile being processed
    if (state.lastProcessedNode() != null) {
      colorTile(state.lastProcessedNode(), TileState.CURRENT);
    }

    for (int i = 0; i < state.finalPath().size(); i++) {
      Coordinate coord = state.finalPath().get(i);
      TileState tileState = TileState.PATH;
      if (i == 0 || i == state.finalPath().size() - 1) { // if last or first
        tileState = TileState.CURRENT;
      }
      colorTile(coord, tileState);
    }
  }

  private static void colorTile(Coordinate coord, TileState state) {
    Tile tile = Game.tileAT(coord);
    if (tile == null) {
      return;
    }

    tile.tintColor(state.color());
  }

  private enum TileState {
    /** Reset the tile color to default. */
    CLEAR(-1), // No color
    /** This tile is part of the open set. */
    OPEN(0x00FF00FF), // Green
    /** This tile is part of the closed set. */
    CLOSED(0xFF0000FF), // Red
    /** This tile is part of the final path. */
    PATH(0x0000FFFF), // Blue
    /** The current tile being processed. */
    CURRENT(0xFFFF00FF); // Yellow

    private final int color;

    TileState(int color) {
      this.color = color;
    }

    /**
     * Get the color of the tile state.
     *
     * @return The color of the tile state.
     */
    public int color() {
      return color;
    }
  }
}
