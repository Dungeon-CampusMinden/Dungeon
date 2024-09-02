package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.utils.functions.IVoidFunction;
import de.fwatermann.dungine.utils.functions.IVoidFunction1Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.joml.Vector3f;

public class RigidBodyComponent extends Component {

  private final List<Collider> colliders = new ArrayList<>();

  private boolean sleeping = false;
  private boolean kinematic = false; // True = object is not affected by forces and gravity
  private boolean gravity = true;

  private float mass = 1.0f;
  private float bounciness = 0.33f;

  private final Vector3f force = new Vector3f();
  private final Vector3f velocity = new Vector3f();

  private IVoidFunction1Parameter<RigidBodyComponent> onCollision = null;
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
    this.force.set(force);
    this.sleeping(false);
    return this;
  }

  public RigidBodyComponent applyForce(Vector3f force) {
    return this.applyForce(force.x, force.y, force.z, ForceMode.FORCE);
  }

  public RigidBodyComponent applyForce(float x, float y, float z) {
    return this.applyForce(x, y, z, ForceMode.FORCE);
  }

  public RigidBodyComponent applyForce(Vector3f force, ForceMode mode) {
    return this.applyForce(force.x, force.y, force.z, mode);
  }

  public RigidBodyComponent applyForce(float x, float y, float z, ForceMode mode) {
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
    this.sleeping(false);
    return this;
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
    this.velocity.set(velocity);
    this.sleeping(false);
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

  public Stream<Collider> colliders() {
    return this.colliders.stream();
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

  public RigidBodyComponent onCollision(IVoidFunction1Parameter<RigidBodyComponent> onCollision) {
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
