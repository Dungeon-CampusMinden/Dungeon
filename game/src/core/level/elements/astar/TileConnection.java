package core.level.elements.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import core.level.Tile;

/**
 * Represents a connection between two tile.
 *
 * @author Marti Stuwe
 */
public class TileConnection implements Connection<Tile> {

  private final Tile from;
  private final Tile to;
  private final float cost;

  /**
   * Create a directed connection between two Tiles.
   *
   * @param from Start-Tile
   * @param to End-Tile
   */
  public TileConnection(Tile from, Tile to) {
    this.from = from;
    this.to = to;
    this.cost = from.distance(to);
  }

  @Override
  public float getCost() {
    return cost;
  }

  @Override
  public Tile getFromNode() {
    return from;
  }

  @Override
  public Tile getToNode() {
    return to;
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass() == TileConnection.class
        && ((TileConnection) obj).from == from
        && ((TileConnection) obj).to == to;
  }
}
