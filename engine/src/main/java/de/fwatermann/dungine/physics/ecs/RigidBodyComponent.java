package de.fwatermann.dungine.physics.ecs;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.utils.functions.IVoidFunction;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.joml.Vector3f;

public class RigidBodyComponent extends Component {

  private final List<Collider> colliders = new ArrayList<>();

  private boolean sleeping = false;
  private boolean kinematic = false; // True = object is not affected by forces and gravity
  private boolean gravity = true;

  private float mass = 1.0f;
  private float bounciness = 0.5f;

  private final Vector3f velocity = new Vector3f();
  private final Vector3f angularVelocity = new Vector3f();
  private final Vector3f force = new Vector3f();
  private final Vector3f torque = new Vector3f();

  private IVoidFunction1P<RigidBodyComponent> onCollision = null;
  private IVoidFunction onSleep = null;
  private IVoidFunction onWake = null;

  public int sleepCounter = 0;

  public RigidBodyComponent() {
    super(false);
  }

  public Vector3f force() {
    return new Vector3f(this.force);
  }

  public RigidBodyComponent force(Vector3f force) {
    return this.force(force, true);
  }

  public RigidBodyComponent force(Vector3f force, boolean wake) {
    this.force.set(force);
    if(wake) {
      this.sleeping(false);
    }
    return this;
  }

  public RigidBodyComponent force(float x, float y, float z) {
    return this.force(x, y, z, true);
  }

  public RigidBodyComponent force(float x, float y, float z, boolean wake) {
    this.force.set(x, y, z);
    if(wake) {
      this.sleeping(false);
    }
    return this;
  }

  public RigidBodyComponent applyForce(Vector3f force) {
    return this.applyForce(force.x, force.y, force.z, ForceMode.FORCE, true);
  }

  public RigidBodyComponent applyForce(float x, float y, float z) {
    return this.applyForce(x, y, z, ForceMode.FORCE, true);
  }

  public RigidBodyComponent applyForce(Vector3f force, ForceMode mode) {
    return this.applyForce(force.x, force.y, force.z, mode, true);
  }

  public RigidBodyComponent applyForce(float x, float y, float z, ForceMode mode, boolean wake) {
    switch(mode) {
      case FORCE:
        this.force.add(x, y, z);
        break;
      case ACCELERATION:
        this.force.add(x * this.mass, y * this.mass, z * this.mass);
        break;
      case IMPULSE:
        this.velocity.add(x / this.mass, y / this.mass, z / this.mass);
        break;
      case VELOCITY_CHANGE:
        this.velocity.add(x, y, z);
        break;
    }
    if(wake) {
      this.sleeping(false);
    }
    return this;
  }

  public RigidBodyComponent applyForceAt(Vector3f force, Vector3f position) {
    return this.applyForceAt(force.x, force.y, force.z, position.x, position.y, position.z, ForceMode.FORCE, true);
  }

  public RigidBodyComponent applyForceAt(float x, float y, float z, float px, float py, float pz) {
    return this.applyForceAt(x, y, z, px, py, pz, ForceMode.FORCE, true);
  }

  public RigidBodyComponent applyForceAt(Vector3f force, Vector3f position, ForceMode mode) {
    return this.applyForceAt(force.x, force.y, force.z, position.x, position.y, position.z, mode, true);
  }

  public RigidBodyComponent applyForceAt(float x, float y, float z, float px, float py, float pz, ForceMode mode) {
    return this.applyForceAt(x, y, z, px, py, pz, mode, true);
  }

  public RigidBodyComponent applyForceAt(Vector3f force, Vector3f position, ForceMode mode, boolean wake) {
    return this.applyForceAt(force.x, force.y, force.z, position.x, position.y, position.z, mode, wake);
  }

  public RigidBodyComponent applyForceAt(float x, float y, float z, float px, float py, float pz, ForceMode mode, boolean wake) {
    Vector3f r = new Vector3f(px, py, pz).sub(this.entity().position());
    Vector3f f = new Vector3f(x, y, z);
    Vector3f torque = new Vector3f();
    torque.cross(r, f);
    this.applyForce(x, y, z, mode, wake);
    this.applyTorque(torque, mode, wake);
    return this;
  }

  public RigidBodyComponent applyTorque(Vector3f torque) {
    return this.applyTorque(torque, ForceMode.FORCE, true);
  }

  public RigidBodyComponent applyTorque(Vector3f torque, ForceMode mode) {
    return this.applyTorque(torque, mode, true);
  }

  public RigidBodyComponent applyTorque(float tX, float tY, float tZ, ForceMode mode, boolean wake) {
    return this.applyTorque(new Vector3f(tX, tY, tZ), mode, wake);
  }

  public RigidBodyComponent applyTorque(Vector3f torque, ForceMode mode, boolean wake) {
    switch(mode) {
      case FORCE:
        this.torque.add(torque);
        break;
      case ACCELERATION:
        this.torque.add(torque.x * this.mass, torque.y * this.mass, torque.z * this.mass);
        break;
      case IMPULSE:
        this.angularVelocity.add(torque.x / this.mass, torque.y / this.mass, torque.z / this.mass);
        break;
      case VELOCITY_CHANGE:
        this.angularVelocity.add(torque);
        break;
    }
    if(wake) {
      this.sleeping(false);
    }
    return this;
  }

  /**
   * Calculates the center of mass based on all colliders attached to this rigid body.
   * @return The center of mass
   */
  public Vector3f getCenterOfMass() {
    Vector3f result = new Vector3f();
    for(Collider collider : this.colliders) {
      result.add(collider.center());
    }
    return result.div(this.colliders.size());
  }

  public boolean sleeping() {
    return this.sleeping;
  }

  public RigidBodyComponent sleeping(boolean sleeping) {
    if(this.sleeping && !sleeping) {
      Optional.ofNullable(this.onWake).ifPresent(IVoidFunction::run);
    } else if(!this.sleeping && sleeping) {
      Optional.ofNullable(this.onSleep).ifPresent(IVoidFunction::run);
    }
    this.sleeping = sleeping;
    this.sleepCounter = 0;
    return this;
  }

  public boolean kinematic() {
    return this.kinematic;
  }

  public RigidBodyComponent kinematic(boolean kinematic) {
    this.kinematic = kinematic;
    return this;
  }

  public boolean gravity() {
    return this.gravity;
  }

  public RigidBodyComponent gravity(boolean gravity) {
    this.gravity = gravity;
    this.sleeping(false);
    return this;
  }

  public float mass() {
    return this.mass;
  }

  public RigidBodyComponent mass(float mass) {
    this.mass = mass;
    this.sleeping(false);
    return this;
  }

  public Vector3f velocity() {
    return this.velocity;
  }

  public RigidBodyComponent velocity(Vector3f velocity) {
    return this.velocity(velocity, true);
  }

  public RigidBodyComponent velocity(Vector3f velocity, boolean wake) {
    this.velocity.set(velocity);
    if(wake) {
      this.sleeping(false);
    }
    return this;
  }

  public void accelerate(Vector3f acceleration) {
    this.velocity.add(acceleration);
    this.sleeping(false);
  }

  public void accelerate(float x, float y, float z) {
    this.velocity.add(x, y, z);
    this.sleeping(false);
  }

  public List<Collider> colliders() {
    return Collections.unmodifiableList(this.colliders);
  }

  public RigidBodyComponent addCollider(Collider collider) {
    this.colliders.add(collider);
    this.sleeping(false);
    return this;
  }

  public RigidBodyComponent removeCollider(Collider collider) {
    this.colliders.remove(collider);
    this.sleeping(false);
    return this;
  }

  public RigidBodyComponent onCollision(IVoidFunction1P<RigidBodyComponent> onCollision) {
    this.onCollision = onCollision;
    return this;
  }

  public void collision(RigidBodyComponent other) {
    if(this.onCollision != null) {
      this.onCollision.run(other);
    }
  }

  public RigidBodyComponent onSleep(IVoidFunction onSleep) {
    this.onSleep = onSleep;
    return this;
  }

  public RigidBodyComponent onWake(IVoidFunction onWake) {
    this.onWake = onWake;
    return this;
  }

  public float bounciness() {
    return this.bounciness;
  }

  public RigidBodyComponent bounciness(float bounciness) {
    this.bounciness = bounciness;
    return this;
  }

  public enum ForceMode {
    FORCE,
    ACCELERATION,
    IMPULSE,
    VELOCITY_CHANGE;
  }

}
