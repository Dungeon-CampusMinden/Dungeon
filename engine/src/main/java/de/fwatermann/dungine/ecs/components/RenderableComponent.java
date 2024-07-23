package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.graphics.IRenderable;

public class RenderableComponent implements Component {

  private IRenderable renderable;

  public RenderableComponent(IRenderable renderable) {
    this.renderable = renderable;
  }

  public IRenderable renderable() {
    return this.renderable;
  }

  public RenderableComponent renderable(IRenderable renderable) {
    this.renderable = renderable;
    return this;
  }

}
