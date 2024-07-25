package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.IRenderable;

public class RenderableComponent extends Component {

  public final IRenderable renderable;

  public RenderableComponent(IRenderable renderable) {
    super(true);
    this.renderable = renderable;
  }

}
