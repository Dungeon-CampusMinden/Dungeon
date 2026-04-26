package core.level.path;

import core.level.Tile;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An immutable {@link TilePath} implementation backed by an ordered {@link List}.
 *
 * <p>The provided tiles are copied during construction. Afterward the path cannot be modified.
 */
public final class ListTilePath implements TilePath {

  /** The internal list holding the tiles that make up this path. */
  private final List<Tile> tiles;

  /** Creates an empty {@code ListTilePath} with no tiles. */
  public ListTilePath() {
    this.tiles = List.of();
  }

  /**
   * Creates a {@code ListTilePath} pre-populated with the tiles from the given collection.
   *
   * <p>The order of the tiles in the new path matches the iteration order of the provided
   * collection.
   *
   * @param tiles the collection of tiles to initialize this path with; must not be {@code null} and
   *     must not contain {@code null} elements
   */
  public ListTilePath(final Collection<Tile> tiles) {
    this.tiles = List.copyOf(tiles);
  }

  /**
   * Returns the number of tiles in this path.
   *
   * @return the number of tiles; never negative
   */
  @Override
  public int size() {
    return tiles.size();
  }

  /**
   * Returns the tile at the specified position in this path.
   *
   * @param index zero-based index of the tile to return
   * @return the tile at the given index
   * @throws IndexOutOfBoundsException if {@code index} is out of range ({@code index < 0 || index
   *     >= size()})
   */
  @Override
  public Tile get(final int index) {
    return tiles.get(index);
  }

  /**
   * Returns an iterator over the tiles in this path in insertion order.
   *
   * @return an {@link Iterator} over the tiles
   */
  @Override
  public Iterator<Tile> iterator() {
    return tiles.iterator();
  }

  /**
   * Returns the immutable tile list backing this path.
   *
   * @return an unmodifiable {@link List} of the tiles in this path
   */
  public List<Tile> asUnmodifiableList() {
    return tiles;
  }
}
