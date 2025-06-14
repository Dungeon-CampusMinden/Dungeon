package aiAdvanced.pathfinding;

/** Enum representing the different states of a tile during the pathfinding process. */
public enum TileState {
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
