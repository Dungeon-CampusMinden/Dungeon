package contrib.components;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import core.Component;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import java.util.List;

/**
 * A PathComponent stores the path that an entity should follow. The path is represented by a {@link
 * GraphPath} of {@link Tile}s. The PathComponent is used by the {@link contrib.systems.PathSystem}
 * to move entities along the path. The path is generated by the {@link contrib.systems.AISystem},
 * which uses the {@link contrib.utils.components.ai.AIUtils} class to calculate the path.
 *
 * <p>If {@link contrib.entities.HeroFactory#ENABLE_MOUSE_MOVEMENT} is set to true, this handles the
 * mouse movement of the hero, by using pathfinding to calculate the path to the mouse click.
 *
 * @see AIComponent
 * @see contrib.systems.AISystem
 * @see core.components.PlayerComponent
 */
public class PathComponent implements Component {
  private GraphPath<Tile> path;

  /**
   * Constructor with a path.
   *
   * @param path The path to be represented by this component.
   */
  public PathComponent(GraphPath<Tile> path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null.");
    }
    this.path = path;
  }

  /**
   * Constructor with a path as a List of Coordinates.
   *
   * @param path The path to be represented by this component.
   */
  public PathComponent(List<Coordinate> path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null.");
    }
    this.path = new DefaultGraphPath<>();
    for (Coordinate coordinate : path) {
      Tile tile = Game.tileAT(coordinate);
      if (tile == null) {
        throw new IllegalArgumentException("Path contains an invalid coordinate: " + coordinate);
      }
      this.path.add(tile);
    }
  }

  /**
   * Getter for the path.
   *
   * @return The path represented by this component.
   */
  public GraphPath<Tile> path() {
    return path;
  }

  /**
   * A PathComponent is valid, if it holds a path with more than zero steps.
   *
   * @return <code>true</code> iff valid; <code>false</code> otherwise
   */
  public boolean isValid() {
    return path.getCount() > 0;
  }

  /**
   * Setter for the path.
   *
   * @param path The new path to be represented by this component.
   */
  public void path(GraphPath<Tile> path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null.");
    }
    this.path = path;
  }

  /** Clears the path represented by this component. */
  public void clear() {
    this.path = new DefaultGraphPath<>();
  }

  @Override
  public String toString() {
    return "PathComponent{" + "path=" + path + '}';
  }
}
