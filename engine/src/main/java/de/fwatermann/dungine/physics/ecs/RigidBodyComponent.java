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

/**
 * The `RigidBodyComponent` class represents a physical body in the ECS that can be affected by
 * forces and collisions. It extends the `Component` class and provides methods to manage physical
 * properties and behaviors.
 */
public class RigidBodyComponent extends Component {

  /** List of colliders attached to this rigid body. */
  private final List<Collider> colliders = new ArrayList<>();

  /** Indicates whether the rigid body is sleeping. */
  private boolean sleeping = false;

  /** Indicates whether the rigid body is kinematic (not affected by forces and gravity). */
  private boolean kinematic = false;

  /** Indicates whether the rigid body is affected by gravity. */
  private boolean gravity = true;

  /** Mass of the rigid body. */
  private float mass = 1.0f;

  /** Bounciness of the rigid body. */
  private float bounciness = 0.5f;

  /** Velocity of the rigid body. */
  private final Vector3f velocity = new Vector3f();

  /** Angular velocity of the rigid body. */
  private final Vector3f angularVelocity = new Vector3f();

  /** Force applied to the rigid body. */
  private final Vector3f force = new Vector3f();

  /** Torque applied to the rigid body. */
  private final Vector3f torque = new Vector3f();

  /** Function to be called on collision. */
  private IVoidFunction1P<RigidBodyComponent> onCollision = null;

  /** Function to be called when the rigid body goes to sleep. */
  private IVoidFunction onSleep = null;

  /** Function to be called when the rigid body wakes up. */
  private IVoidFunction onWake = null;

  /** Counter for sleep state. */
  public int sleepCounter = 0;

  /** Constructs a new `RigidBodyComponent`. */
  public RigidBodyComponent() {
    super(false);
  }

  /**
   * Gets the force applied to the rigid body.
   *
   * @return the force applied to the rigid body
   */
  public Vector3f force() {
    return new Vector3f(this.force);
  }

  /**
   * Sets the force applied to the rigid body.
   *
   * @param force the force to apply
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent force(Vector3f force) {
    return this.force(force, true);
  }

  /**
   * Sets the force applied to the rigid body and optionally wakes it up.
   *
   * @param force the force to apply
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent force(Vector3f force, boolean wake) {
    this.force.set(force);
    if (wake) {
      this.sleeping(false);
    }
    return this;
  }

  /**
   * Sets the force applied to the rigid body.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent force(float x, float y, float z) {
    return this.force(x, y, z, true);
  }

  /**
   * Sets the force applied to the rigid body and optionally wakes it up.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent force(float x, float y, float z, boolean wake) {
    this.force.set(x, y, z);
    if (wake) {
      this.sleeping(false);
    }
    return this;
  }

  /**
   * Applies a force to the rigid body.
   *
   * @param force the force to apply
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForce(Vector3f force) {
    return this.applyForce(force.x, force.y, force.z, ForceMode.FORCE, true);
  }

  /**
   * Applies a force to the rigid body.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForce(float x, float y, float z) {
    return this.applyForce(x, y, z, ForceMode.FORCE, true);
  }

  /**
   * Applies a force to the rigid body with a specified mode.
   *
   * @param force the force to apply
   * @param mode the mode of the force
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForce(Vector3f force, ForceMode mode) {
    return this.applyForce(force.x, force.y, force.z, mode, true);
  }

  /**
   * Applies a force to the rigid body with a specified mode and optionally wakes it up.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @param mode the mode of the force
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForce(float x, float y, float z, ForceMode mode, boolean wake) {
    switch (mode) {
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
    if (wake) {
      this.sleeping(false);
    }
    return this;
  }

  /**
   * Applies a force at a specific position on the rigid body.
   *
   * @param force the force to apply
   * @param position the position to apply the force
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForceAt(Vector3f force, Vector3f position) {
    return this.applyForceAt(
        force.x, force.y, force.z, position.x, position.y, position.z, ForceMode.FORCE, true);
  }

  /**
   * Applies a force at a specific position on the rigid body.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @param px the x component of the position
   * @param py the y component of the position
   * @param pz the z component of the position
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForceAt(float x, float y, float z, float px, float py, float pz) {
    return this.applyForceAt(x, y, z, px, py, pz, ForceMode.FORCE, true);
  }

  /**
   * Applies a force at a specific position on the rigid body with a specified mode.
   *
   * @param force the force to apply
   * @param position the position to apply the force
   * @param mode the mode of the force
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForceAt(Vector3f force, Vector3f position, ForceMode mode) {
    return this.applyForceAt(
        force.x, force.y, force.z, position.x, position.y, position.z, mode, true);
  }

  /**
   * Applies a force at a specific position on the rigid body with a specified mode.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @param px the x component of the position
   * @param py the y component of the position
   * @param pz the z component of the position
   * @param mode the mode of the force
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForceAt(
      float x, float y, float z, float px, float py, float pz, ForceMode mode) {
    return this.applyForceAt(x, y, z, px, py, pz, mode, true);
  }

  /**
   * Applies a force at a specific position on the rigid body with a specified mode and optionally
   * wakes it up.
   *
   * @param force the force to apply
   * @param position the position to apply the force
   * @param mode the mode of the force
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForceAt(
      Vector3f force, Vector3f position, ForceMode mode, boolean wake) {
    return this.applyForceAt(
        force.x, force.y, force.z, position.x, position.y, position.z, mode, wake);
  }

  /**
   * Applies a force at a specific position on the rigid body with a specified mode and optionally
   * wakes it up.
   *
   * @param x the x component of the force
   * @param y the y component of the force
   * @param z the z component of the force
   * @param px the x component of the position
   * @param py the y component of the position
   * @param pz the z component of the position
   * @param mode the mode of the force
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyForceAt(
      float x, float y, float z, float px, float py, float pz, ForceMode mode, boolean wake) {
    Vector3f r = new Vector3f(px, py, pz).sub(this.entity().position());
    Vector3f f = new Vector3f(x, y, z);
    Vector3f torque = new Vector3f();
    torque.cross(r, f);
    this.applyForce(x, y, z, mode, wake);
    this.applyTorque(torque, mode, wake);
    return this;
  }

  /**
   * Applies torque to the rigid body.
   *
   * @param torque the torque to apply
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyTorque(Vector3f torque) {
    return this.applyTorque(torque, ForceMode.FORCE, true);
  }

  /**
   * Applies torque to the rigid body with a specified mode.
   *
   * @param torque the torque to apply
   * @param mode the mode of the torque
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyTorque(Vector3f torque, ForceMode mode) {
    return this.applyTorque(torque, mode, true);
  }

  /**
   * Applies torque to the rigid body with a specified mode and optionally wakes it up.
   *
   * @param tX the x component of the torque
   * @param tY the y component of the torque
   * @param tZ the z component of the torque
   * @param mode the mode of the torque
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyTorque(
      float tX, float tY, float tZ, ForceMode mode, boolean wake) {
    return this.applyTorque(new Vector3f(tX, tY, tZ), mode, wake);
  }

  /**
   * Applies torque to the rigid body with a specified mode and optionally wakes it up.
   *
   * @param torque the torque to apply
   * @param mode the mode of the torque
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent applyTorque(Vector3f torque, ForceMode mode, boolean wake) {
    switch (mode) {
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
    if (wake) {
      this.sleeping(false);
    }
    return this;
  }

  /**
   * Calculates the center of mass based on all colliders attached to this rigid body.
   *
   * @return the center of mass
   */
  public Vector3f getCenterOfMass() {
    Vector3f result = new Vector3f();
    for (Collider collider : this.colliders) {
      result.add(collider.center());
    }
    return result.div(this.colliders.size());
  }

  /**
   * Checks if the rigid body is sleeping.
   *
   * @return `true` if the rigid body is sleeping, `false` otherwise
   */
  public boolean sleeping() {
    return this.sleeping;
  }

  /**
   * Sets the sleeping state of the rigid body.
   *
   * @param sleeping the new sleeping state
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent sleeping(boolean sleeping) {
    if (this.sleeping && !sleeping) {
      Optional.ofNullable(this.onWake).ifPresent(IVoidFunction::run);
    } else if (!this.sleeping && sleeping) {
      Optional.ofNullable(this.onSleep).ifPresent(IVoidFunction::run);
    }
    this.sleeping = sleeping;
    this.sleepCounter = 0;
    return this;
  }

  /**
   * Checks if the rigid body is kinematic.
   *
   * @return `true` if the rigid body is kinematic, `false` otherwise
   */
  public boolean kinematic() {
    return this.kinematic;
  }

  /**
   * Sets the kinematic state of the rigid body.
   *
   * @param kinematic the new kinematic state
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent kinematic(boolean kinematic) {
    this.kinematic = kinematic;
    return this;
  }

  /**
   * Checks if the rigid body is affected by gravity.
   *
   * @return `true` if the rigid body is affected by gravity, `false` otherwise
   */
  public boolean gravity() {
    return this.gravity;
  }

  /**
   * Sets the gravity state of the rigid body.
   *
   * @param gravity the new gravity state
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent gravity(boolean gravity) {
    this.gravity = gravity;
    this.sleeping(false);
    return this;
  }

  /**
   * Gets the mass of the rigid body.
   *
   * @return the mass of the rigid body
   */
  public float mass() {
    return this.mass;
  }

  /**
   * Sets the mass of the rigid body.
   *
   * @param mass the new mass
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent mass(float mass) {
    this.mass = mass;
    this.sleeping(false);
    return this;
  }

  /**
   * Gets the velocity of the rigid body.
   *
   * @return the velocity of the rigid body
   */
  public Vector3f velocity() {
    return this.velocity;
  }

  /**
   * Sets the velocity of the rigid body.
   *
   * @param velocity the new velocity
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent velocity(Vector3f velocity) {
    return this.velocity(velocity, true);
  }

  /**
   * Sets the velocity of the rigid body and optionally wakes it up.
   *
   * @param velocity the new velocity
   * @param wake whether to wake up the rigid body
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent velocity(Vector3f velocity, boolean wake) {
    this.velocity.set(velocity);
    if (wake) {
      this.sleeping(false);
    }
    return this;
  }

  /**
   * Accelerates the rigid body by the specified acceleration.
   *
   * @param acceleration the acceleration to apply
   */
  public void accelerate(Vector3f acceleration) {
    this.velocity.add(acceleration);
    this.sleeping(false);
  }

  /**
   * Accelerates the rigid body by the specified acceleration.
   *
   * @param x the x component of the acceleration
   * @param y the y component of the acceleration
   * @param z the z component of the acceleration
   */
  public void accelerate(float x, float y, float z) {
    this.velocity.add(x, y, z);
    this.sleeping(false);
  }

  /**
   * Gets the list of colliders attached to this rigid body.
   *
   * @return the list of colliders
   */
  public List<Collider> colliders() {
    return Collections.unmodifiableList(this.colliders);
  }

  /**
   * Adds a collider to the rigid body.
   *
   * @param collider the collider to add
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent addCollider(Collider collider) {
    this.colliders.add(collider);
    this.sleeping(false);
    return this;
  }

  /**
   * Removes a collider from the rigid body.
   *
   * @param collider the collider to remove
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent removeCollider(Collider collider) {
    this.colliders.remove(collider);
    this.sleeping(false);
    return this;
  }

  /**
   * Sets the function to be called on collision.
   *
   * @param onCollision the function to call on collision
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent onCollision(IVoidFunction1P<RigidBodyComponent> onCollision) {
    this.onCollision = onCollision;
    return this;
  }

  /**
   * Calls the collision function with another rigid body.
   *
   * @param other the other rigid body
   */
  public void collision(RigidBodyComponent other) {
    if (this.onCollision != null) {
      this.onCollision.run(other);
    }
  }

  /**
   * Sets the function to be called when the rigid body goes to sleep.
   *
   * @param onSleep the function to call when the rigid body goes to sleep
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent onSleep(IVoidFunction onSleep) {
    this.onSleep = onSleep;
    return this;
  }

  /**
   * Sets the function to be called when the rigid body wakes up.
   *
   * @param onWake the function to call when the rigid body wakes up
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent onWake(IVoidFunction onWake) {
    this.onWake = onWake;
    return this;
  }

  /**
   * Gets the bounciness of the rigid body.
   *
   * @return the bounciness of the rigid body
   */
  public float bounciness() {
    return this.bounciness;
  }

  /**
   * Sets the bounciness of the rigid body.
   *
   * @param bounciness the new bounciness
   * @return this `RigidBodyComponent` instance for method chaining
   */
  public RigidBodyComponent bounciness(float bounciness) {
    this.bounciness = bounciness;
    return this;
  }

  /**
   * Enum representing the mode of force application. Each mode defines a different way to apply
   * forces to a rigid body.
   */
  public enum ForceMode {
    /** Apply a continuous force to the rigid body. */
    FORCE,

    /** Apply an acceleration to the rigid body. */
    ACCELERATION,

    /** Apply an instantaneous change in velocity to the rigid body. */
    IMPULSE,

    /** Apply an instantaneous change in velocity without considering mass. */
    VELOCITY_CHANGE
  }
}
