package core.level.path;

import core.level.Tile;
import core.level.elements.ILevel;
import java.util.Optional;

/**
 * Service interface for pathfinding operations within a level.
 *
 * <p>The PathfindingService provides methods to compute a path from a starting {@link Tile} to a
 * destination {@link Tile} within a given {@link ILevel}.
 *
 * <p>Pathfinding logic and algorithms are defined by specific implementations of this interface.
 *
 * <p>Key responsibilities of the PathfindingService include:
 *
 * <ul>
 *   <li>Determining the sequence of tiles (as a {@link TilePath}) that form the shortest, most
 *       optimal, or desired path from a start tile to an end tile.
 *   <li>Supporting scenarios where no valid path exists via the use of {@link Optional}.
 * </ul>
 *
 * <p>Classes implementing this interface should be prepared to handle levels with varying
 * topologies, obstacles, and constraints as defined by the {@link ILevel} provided.
 */
public interface PathfindingService {

  /**
   * Computes a path from a starting tile to a destination tile within the context of a given level.
   *
   * @param level the level in which the pathfinding operation is to be performed; cannot be null
   * @param start the starting tile from which the path should begin; cannot be null
   * @param end the destination tile to which the path should lead; cannot be null
   * @return an {@link Optional} containing the computed {@link TilePath} if a valid path exists, or
   *     an empty {@link Optional} if no path can be found
   */
  Optional<TilePath> findPath(ILevel level, Tile start, Tile end);
}
