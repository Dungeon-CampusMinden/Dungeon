package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RenderableSystem extends System<RenderableSystem> {

  private static final Logger LOGGER = LogManager.getLogger(RenderableSystem.class);

  private Camera<?> camera;
  private int latestRenderCount = 0;
  private int renderCount = 0;

  public RenderableSystem(Camera<?> camera) {
    super(1, true, RenderableComponent.class);
    this.camera = camera;
  }

  @Override
  public void update(ECS ecs) {
    ecs.entities(s -> s.forEach(e -> {
      e.components(RenderableComponent.class).forEach(c -> {
        c.renderable.transformation(e.position(), e.rotation(), e.size());

        if(this.camera instanceof CameraPerspective pCam) {
          if(!c.renderable.shouldRender(pCam.frustum())) return;
        }
        c.renderable.render(this.camera);
        this.renderCount ++;
      });
    }), RenderableComponent.class);
    this.latestRenderCount = this.renderCount;
    this.renderCount = 0;
  }

  public Camera<?> camera() {
    return this.camera;
  }

  public RenderableSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }

  public int latestRenderCount() {
    return this.latestRenderCount;
  }
}
