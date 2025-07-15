package core.level.elements.astar;

import com.badlogic.gdx.ai.pfa.Heuristic;
import core.level.Tile;

/**
 * @author Marti Stuwe
 */
public class TileHeuristic implements Heuristic<Tile> {

  /**
   * Heuristic used by the pathfinding algorithm.
   *
   * @param start From
   * @param goal To
   * @return Manhattan Distance between from and to tile
   */
  @Override
  public float estimate(Tile start, Tile goal) {
    return start.distance(goal);
  }
}
