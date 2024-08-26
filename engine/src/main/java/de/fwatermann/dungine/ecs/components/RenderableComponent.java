package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.Renderable;

public class RenderableComponent extends Component {

  public final Renderable<?> renderable;

  public RenderableComponent(Renderable<?> renderable) {
    super(true);
    this.renderable = renderable;
  }

}
