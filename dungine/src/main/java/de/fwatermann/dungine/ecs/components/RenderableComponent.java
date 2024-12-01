package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.Renderable;

/**
 * The `RenderableComponent` class represents a component that holds a renderable object. It extends
 * the `Component` class and provides methods to manage the renderable object.
 */
public class RenderableComponent extends Component {

  /** The renderable object associated with this component. */
  public final Renderable<?> renderable;

  /**
   * Constructs a new `RenderableComponent` with the specified renderable object.
   *
   * @param renderable the renderable object to be associated with this component
   */
  public RenderableComponent(Renderable<?> renderable) {
    super(true);
    this.renderable = renderable;
  }
}
