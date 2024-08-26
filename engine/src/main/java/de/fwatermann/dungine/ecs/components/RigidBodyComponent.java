package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import org.joml.Vector3f;

public class RigidBodyComponent extends Component {

  private boolean sleeping = false;
  private boolean kinematic = false;

  private final Vector3f velocity = new Vector3f();
  private final Vector3f force = new Vector3f();

  public RigidBodyComponent() {
    super(false);
  }

  public Vector3f velocity() {
    return new Vector3f(this.velocity);
  }

  public Vector3f force() {
    return this.force;
  }

  public RigidBodyComponent applyForce(Vector3f force) {
    this.force.add(force);
    return this;
  }

  public RigidBodyComponent applyForce(float x, float y, float z) {
    this.force.add(x, y, z);
    return this;
  }

  public boolean sleeping() {
    return this.sleeping;
  }

  public RigidBodyComponent sleeping(boolean sleeping) {
    this.sleeping = sleeping;
    return this;
  }

  public boolean kinematic() {
    return this.kinematic;
  }

  public RigidBodyComponent kinematic(boolean kinematic) {
    this.kinematic = kinematic;
    return this;
  }

  public void update(float dt) {
    if (!this.kinematic) {
      this.velocity.add(this.force.mul(dt));
      this.force.zero();
    }
  }

}
