package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.RigidBodyComponent;
import java.util.Optional;

public class PhysicsSystem extends System<PhysicsSystem> {

  public static final float DEFAULT_GRAVITY_CONSTANT = 9.81f;

  private float gravityConstant;
  private long lastExecution = java.lang.System.currentTimeMillis();

  public PhysicsSystem(float gravityConstant) {
    super(0);
    this.gravityConstant = gravityConstant;
  }

  public PhysicsSystem() {
    this(DEFAULT_GRAVITY_CONSTANT);
  }

  @Override
  public void update(ECS ecs) {
    float deltaTime = (java.lang.System.currentTimeMillis() - this.lastExecution) / 1000.0f;
    ecs.entities((s) -> s.forEach(
            e -> {
              Optional<RigidBodyComponent> opt = e.component(RigidBodyComponent.class);
              if (opt.isEmpty()) return;
              RigidBodyComponent rb = opt.get();

              if (rb.gravity()) {
                rb.applyForce(0, -this.gravityConstant * deltaTime, 0);
              }

              e.position().add(rb.force().mul(deltaTime));
            }), RigidBodyComponent.class);
    this.lastExecution = java.lang.System.currentTimeMillis();
  }
}
