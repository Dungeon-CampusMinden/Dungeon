package core.level.path;

import core.level.Tile;
import java.util.Iterator;

/**
 * Engine-agnostic path representation for tile-based navigation.
 *
 * <p>This is intentionally minimal: the AI only needs indexing + iteration.
 */
public interface TilePath extends Iterable<Tile> {
  int size();

  Tile get(int index);

  @Override
  Iterator<Tile> iterator();

  default boolean isEmpty() {
    return size() <= 0;
  }

  default Tile last() {
    return isEmpty() ? null : get(size() - 1);
  }
}
