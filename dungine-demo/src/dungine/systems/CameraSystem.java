package dungine.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.graphics.camera.Camera;
import dungine.components.CameraComponent;

/**
 * The `CameraSystem` is a System that updates the camera position and lookAt based on the position
 * of the entity with a CameraComponent.
 */
public class CameraSystem extends System<CameraSystem> {

  private Camera<?> camera;

  /**
   * Create a new `CameraSystem` instance.
   *
   * @param camera The camera instance.
   */
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

  /**
   * Get the camera used by this CameraSystem.
   *
   * @return The camera instance.
   */
  public Camera<?> camera() {
    return this.camera;
  }

  /**
   * Set the camera used by this CameraSystem.
   *
   * @param camera The camera instance.
   * @return This CameraSystem instance.
   */
  public CameraSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }
}
