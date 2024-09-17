package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.LightComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.scene.SceneRenderer;
import de.fwatermann.dungine.graphics.scene.light.Light;
import de.fwatermann.dungine.graphics.scene.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

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
    Set<Model> models = new HashSet<>();
    Set<Light<?>> lights = new HashSet<>();
    ecs.forEachEntity(e -> {
      e.components(RenderableComponent.class).forEach(c -> {
        c.renderable.transformation(e.position(), e.rotation(), e.size());

        if(this.camera instanceof CameraPerspective pCam) {
          if(!c.renderable.shouldRender(pCam.frustum())) return;
        }
        if(c.renderable instanceof Model model) {
          models.add(model);
        } else {
          c.renderable.render(this.camera);
        }
        this.renderCount ++;
      });
      e.components(LightComponent.class).forEach(c -> {
        lights.add(c.light());
      });
    });
    this.latestRenderCount = this.renderCount;
    this.renderCount = 0;

    SceneRenderer.renderScene(this.camera, models, lights);

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
