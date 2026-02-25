package core.level.path;

import core.level.Tile;
import java.util.*;

/**
 * A {@link TilePath} implementation that stores tiles in an ordered {@link List}.
 *
 * <p>Tiles are kept in insertion order and can be accessed by index, iterated over, or retrieved
 * as an unmodifiable list view.
 */
public final class ListTilePath implements TilePath {

  /** The internal list holding the tiles that make up this path. */
  private final List<Tile> tiles;

  /**
   * Creates an empty {@code ListTilePath} with no tiles.
   */
  public ListTilePath() {
    this.tiles = new ArrayList<>();
  }

  /**
   * Creates a {@code ListTilePath} pre-populated with the tiles from the given collection.
   *
   * <p>The order of the tiles in the new path matches the iteration order of the provided
   * collection.
   *
   * @param tiles the collection of tiles to initialize this path with; must not be {@code null}
   */
  public ListTilePath(final Collection<Tile> tiles) {
    this.tiles = new ArrayList<>(tiles);
  }

  /**
   * Appends the specified tile to the end of this path.
   *
   * @param tile the tile to add; must not be {@code null}
   */
  public void add(final Tile tile) {
    tiles.add(tile);
  }

  /**
   * Removes all tiles from this path.
   *
   * <p>After this call the path will be empty.
   */
  public void clear() {
    tiles.clear();
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
   * @throws IndexOutOfBoundsException if {@code index} is out of range
   *     ({@code index < 0 || index >= size()})
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
   * Returns an unmodifiable view of the underlying tile list.
   *
   * <p>The returned list reflects any subsequent changes to this path, but does not permit
   * direct modification.
   *
   * @return an unmodifiable {@link List} of the tiles in this path
   */
  public List<Tile> asUnmodifiableList() {
    return Collections.unmodifiableList(tiles);
  }
}
