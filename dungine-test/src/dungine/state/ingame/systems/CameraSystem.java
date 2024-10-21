package dungine.state.ingame.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.input.Mouse;
import dungine.state.ingame.components.CameraComponent;
import org.joml.Vector2i;
import org.joml.Vector3f;

/**
 * System responsible for handling camera updates based on mouse input.
 */
public class CameraSystem extends System<CameraSystem> {

  /**
   * Constructs a new CameraSystem.
   */
  public CameraSystem() {
    super(0, true, CameraComponent.class);
  }

  private Vector2i lastMousePos = Mouse.getMousePosition();

  /**
   * Updates the camera system, processing mouse input and adjusting camera positions.
   *
   * @param ecs the ECS instance
   */
  @Override
  public void update(ECS ecs) {

    Vector2i mousePos = Mouse.getMousePosition();
    Vector2i delta = mousePos.sub(this.lastMousePos, new Vector2i());
    this.lastMousePos = mousePos;
    boolean left = (mousePos.x != 0 || mousePos.y != 0) && Mouse.buttonPressed(0); //TODO: KEY BINDINGS!!!

    ecs.forEachEntity(
        (entity) -> {
          entity
              .component(CameraComponent.class)
              .ifPresent(
                  (cc) -> {
                    Camera<?> camera = cc.camera();
                    Vector3f pos = camera.position();

                    // Rotation
                    if (left) {
                      cc.offset().rotateY(delta.x * -0.01f);
                      Vector3f axis = new Vector3f(0, 1, 0).cross(cc.offset()).normalize();
                      cc.offset().rotateAxis(delta.y * -0.01f, axis.x, axis.y, axis.z);
                    }

                    Vector3f target =
                        new Vector3f(cc.offset()).mul(cc.offsetLength()).add(entity.position());
                    Vector3f relative = target.sub(pos, new Vector3f());
                    float dist = relative.length();
                    camera.move(relative.mul(left ? dist / 2.0f : dist / 100.0f));
                    camera.lookAt(entity.position());
                    camera.update();
                  });
        },
        CameraComponent.class);
  }

}
