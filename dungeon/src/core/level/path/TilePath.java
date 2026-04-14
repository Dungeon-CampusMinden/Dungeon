package core.level.path;

import core.level.Tile;
import java.util.Iterator;

/**
 * Interface representing a sequence/path of tiles.
 *
 * <p>TilePath provides an ordered collection of tiles that form a path through a level.
 * It supports iteration, indexing, and convenience methods for path manipulation.
 *
 * <p>Key characteristics:
 * <ul>
 *   <li>Ordered sequence of tiles from start to goal
 *   <li>Random access via index
 *   <li>Iterable for convenient iteration
 *   <li>Empty path support
 * </ul>
 *
 * <p>Implementations should typically be immutable or at least not modified after creation.
 */
public interface TilePath extends Iterable<Tile> {

  /**
   * Gets the number of tiles in this path.
   *
   * @return the number of tiles, or 0 for an empty path
   */
  int size();

  /**
   * Gets the tile at the specified index in this path.
   *
   * <p>Valid indices range from 0 to size() - 1.
   *
   * @param index the index of the tile to retrieve
   * @return the tile at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  Tile get(int index);

  /**
   * Checks whether this path is empty (contains no tiles).
   *
   * @return true if the path contains no tiles, false otherwise
   */
  default boolean isEmpty() {
    return size() <= 0;
  }

  /**
   * Gets the last tile in this path (the destination/goal tile).
   *
   * @return the last tile in the path, or null if the path is empty
   */
  default Tile last() {
    return isEmpty() ? null : get(size() - 1);
  }

  @Override
  Iterator<Tile> iterator();
}
