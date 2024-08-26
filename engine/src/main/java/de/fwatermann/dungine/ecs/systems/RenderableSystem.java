package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.graphics.camera.Camera;

public class RenderableSystem extends System<RenderableSystem> {

  private Camera<?> camera;

  public RenderableSystem(Camera<?> camera) {
    super(1, true, RenderableComponent.class);
    this.camera = camera;
  }

  @Override
  public void update(ECS ecs) {
    ecs.entities(s -> s.forEach(e -> {
      e.components(RenderableComponent.class).forEach(c -> {
        c.renderable.transformation(e.position(), e.rotation(), e.size());
        c.renderable.render(this.camera);
      });
    }), RenderableComponent.class);
  }

  public Camera<?> camera() {
    return this.camera;
  }

  public RenderableSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }
}
