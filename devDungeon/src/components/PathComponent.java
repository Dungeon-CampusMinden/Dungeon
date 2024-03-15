package components;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.AIComponent;
import core.Component;
import core.level.Tile;

/**
 * The PathComponent class implements the Component interface. It represents a path in the game,
 * which is a sequence of tiles.
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
    this.path = path;
  }

  /**
   * Getter for the path.
   *
   * @return The path represented by this component.
   */
  public GraphPath<Tile> path() {
    return this.path;
  }

  /**
   * Setter for the path.
   *
   * @param path The new path to be represented by this component.
   */
  public void path(GraphPath<Tile> path) {
    this.path = path;
  }

  /** Clears the path represented by this component. */
  public void clear() {
    this.path = null;
  }

  @Override
  public String toString() {
    return "PathComponent{" + "path=" + this.path + '}';
  }
}
