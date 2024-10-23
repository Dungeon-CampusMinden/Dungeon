package dungine.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.graphics.camera.Camera;
import dungine.components.CameraComponent;

public class CameraSystem extends System<CameraSystem> {

  private Camera<?> camera;

  public CameraSystem(Camera<?> camera) {
    super(1, true);
    this.camera = camera;
  }

  @Override
  public void update(ECS ecs) {
    ecs.entities(
        stream -> {
          stream
              .findAny()
              .ifPresentOrElse(
                  e -> {
                    this.camera.position(e.position());
                    this.camera.move(0, 5, 2);
                    this.camera.lookAt(e.position());
                  },
                  () -> {
                    this.camera.position(0, 5, 2);
                    this.camera.lookAt(0, 0, 0);
                  });
        },
        CameraComponent.class);
  }

  public Camera<?> camera() {
    return this.camera;
  }

  public CameraSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }
}
