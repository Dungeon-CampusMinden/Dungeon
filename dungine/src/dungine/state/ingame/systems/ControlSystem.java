package dungine.state.ingame.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import dungine.state.ingame.components.CameraComponent;
import dungine.state.ingame.components.ControlComponent;
import java.util.Optional;
import org.joml.Vector3f;

/**
 * System responsible for handling control inputs and updating entity positions accordingly.
 */
public class ControlSystem extends System<ControlSystem> {

  /**
   * Constructs a new ControlSystem.
   */
  public ControlSystem() {
    super(0, true, ControlComponent.class);
  }

  private long lastExecution;

  /**
   * Updates the control system, processing input and moving entities.
   *
   * @param ecs the ECS instance
   */
  @Override
  public void update(ECS ecs) {

    float deltaTime = (java.lang.System.currentTimeMillis() - this.lastExecution) / 1000.0f;
    this.lastExecution = java.lang.System.currentTimeMillis();

    ecs.forEachEntity(
        (entity) -> {

          // Is RigidBody -> Control by forces
          Optional<RigidBodyComponent> rbOpt = entity.component(RigidBodyComponent.class);
          if (rbOpt.isPresent()) {
            return;
          }

          Vector3f forward = new Vector3f(0.0f, 0.0f, -1.0f);
          Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);

          entity.component(CameraComponent.class).ifPresent(cc -> {
            forward.set(cc.offset()).negate().mul(1.0f, 0.0f, 1.0f).normalize();
            right.set(forward).cross(new Vector3f(0.0f, 1.0f, 0.0f)).normalize();
          });

          Vector3f movement = new Vector3f(0.0f);
          if (Keyboard.keyPressed(87)) { // W TODO: Make configurable
            movement.add(forward);
          }
          if (Keyboard.keyPressed(83)) { // S
            movement.sub(forward);
          }
          if (Keyboard.keyPressed(65)) { // A
            movement.sub(right);
          }
          if (Keyboard.keyPressed(68)) { // D
            movement.add(right);
          }

          if(movement.lengthSquared() == 0.0f) {
            return;
          }

          movement.normalize().mul(deltaTime);
          entity.position().add(movement);
        },
        ControlComponent.class);
  }
}
