package contrib.components;

import core.Component;
import core.Game;
import core.level.Tile;
import core.level.path.ListTilePath;
import core.level.path.TilePath;
import core.level.utils.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 * Component that stores a {@link TilePath} for an entity, typically used to define a movement path
 * through the dungeon level.
 *
 * <p>The path can be provided either as a {@link TilePath} directly or as a list of {@link
 * Coordinate} objects, which are then resolved to {@link Tile} instances via the current game
 * state.
 */
public class PathComponent implements Component {
  private TilePath path;

  /**
   * Creates a new {@code PathComponent} from a list of {@link Coordinate} objects.
   *
   * <p>Each coordinate is resolved to the corresponding {@link Tile} in the current level via
   * {@link Game#tileAt(Coordinate)}.
   *
   * @param coordinates the list of coordinates representing the path; must not be {@code null}
   * @throws IllegalArgumentException if {@code coordinates} is {@code null} or contains a
   *     coordinate that does not correspond to a valid tile in the current level
   */
  public PathComponent(final List<Coordinate> coordinates) {
    if (coordinates == null) throw new IllegalArgumentException("Path cannot be null.");

    List<Tile> tiles = new ArrayList<>(coordinates.size());
    for (Coordinate c : coordinates) {
      Tile tile = Game.tileAt(c).orElse(null);
      if (tile == null) {
        throw new IllegalArgumentException("Path contains an invalid coordinate: " + c);
      }
      tiles.add(tile);
    }
    this.path = new ListTilePath(tiles);
  }

  /**
   * Returns the current {@link TilePath} stored in this component.
   *
   * @return the tile path
   */
  public TilePath path() {
    return path;
  }

  /**
   * Sets a new {@link TilePath} for this component.
   *
   * @param path the new tile path; must not be {@code null}
   * @throws IllegalArgumentException if {@code path} is {@code null}
   */
  public void path(final TilePath path) {
    if (path == null) throw new IllegalArgumentException("Path cannot be null.");
    this.path = path;
  }

  /**
   * Checks whether this component holds a valid, non-empty path.
   *
   * @return {@code true} if the path is not {@code null} and contains at least one tile; {@code
   *     false} otherwise
   */
  public boolean isValid() {
    return path != null && !path.isEmpty();
  }

  /**
   * Clears the current path by replacing it with an empty {@link ListTilePath}.
   *
   * <p>After calling this method, {@link #isValid()} will return {@code false}.
   */
  public void clear() {
    this.path = new ListTilePath();
  }

  /**
   * Returns a string representation of this component, including the number of tiles in the path.
   *
   * @return a string in the format {@code PathComponent{path(size)=N}}, where {@code N} is the
   *     path size or {@code -1} if the path is {@code null}
   */
  @Override
  public String toString() {
    return "PathComponent{" + "path(size)=" + (path == null ? -1 : path.size()) + '}';
  }
}
