package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.TextComponent;
import de.fwatermann.dungine.graphics.camera.Camera;

public class Render3DTextSystem extends System<Render3DTextSystem> {

  private Camera<?> camera;

  public Render3DTextSystem(Camera<?> camera) {
    super(0, true);
    this.camera = camera;
  }

  @Override
  public void update(ECS ecs) {
    ecs.forEachEntity(e -> {
      e.components(TextComponent.class)
        .forEach(
          c -> {
            c.render(this.camera, e);
          });
    }, TextComponent.class);
  }

  public Camera<?> camera() {
    return this.camera;
  }

  public Render3DTextSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }
}
