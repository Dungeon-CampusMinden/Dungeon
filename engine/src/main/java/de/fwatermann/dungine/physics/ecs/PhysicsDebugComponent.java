package de.fwatermann.dungine.physics.ecs;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class PhysicsDebugComponent extends Component {

  private boolean displayVelocity = false;
  private boolean displayForce = false;
  private boolean displayPosition = false;
  private boolean displayBoundingBox = false;
  private boolean displayColliders = false;
  private boolean displayCollisionPairs = false;
  private boolean displayEntityPositions = false;

  ReentrantReadWriteLock collisionLock = new ReentrantReadWriteLock();
  private List<Entity> collision = new ArrayList<>();

  public PhysicsDebugComponent(
      boolean displayVelocity,
      boolean displayForce,
      boolean displayPosition,
      boolean displayBoundingBox,
      boolean displayColliders,
      boolean displayCollisionPairs,
      boolean displayEntityPositions) {
    super(false);
    this.displayVelocity = displayVelocity;
    this.displayForce = displayForce;
    this.displayPosition = displayPosition;
    this.displayBoundingBox = displayBoundingBox;
    this.displayColliders = displayColliders;
    this.displayCollisionPairs = displayCollisionPairs;
    this.displayEntityPositions = displayEntityPositions;
  }

  public PhysicsDebugComponent(boolean all) {
    super(false);
    this.displayVelocity = all;
    this.displayForce = all;
    this.displayPosition = all;
    this.displayBoundingBox = all;
    this.displayColliders = all;
    this.displayCollisionPairs = all;
    this.displayEntityPositions = all;
  }

  public PhysicsDebugComponent() {
    super(false);
  }

  public boolean displayVelocity() {
    return this.displayVelocity;
  }

  public PhysicsDebugComponent displayVelocity(boolean displayVelocity) {
    this.displayVelocity = displayVelocity;
    return this;
  }

  public boolean displayForce() {
    return this.displayForce;
  }

  public PhysicsDebugComponent displayForce(boolean displayForce) {
    this.displayForce = displayForce;
    return this;
  }

  public boolean displayPosition() {
    return this.displayPosition;
  }

  public PhysicsDebugComponent displayPosition(boolean displayPosition) {
    this.displayPosition = displayPosition;
    return this;
  }

  public boolean displayBoundingBox() {
    return this.displayBoundingBox;
  }

  public PhysicsDebugComponent displayBoundingBox(boolean displayBoundingBox) {
    this.displayBoundingBox = displayBoundingBox;
    return this;
  }

  public boolean displayColliders() {
    return this.displayColliders;
  }

  public PhysicsDebugComponent displayColliders(boolean displayColliders) {
    this.displayColliders = displayColliders;
    return this;
  }

  public boolean displayCollisionPairs() {
    return this.displayCollisionPairs;
  }

  public PhysicsDebugComponent displayCollisionPairs(boolean displayCollisionPairs) {
    this.displayCollisionPairs = displayCollisionPairs;
    return this;
  }

  public boolean displayEntityPosition() {
    return this.displayEntityPositions;
  }

  public PhysicsDebugComponent displayEntityPosition(boolean displayEntityPositions) {
    this.displayEntityPositions = displayEntityPositions;
    return this;
  }

  public void addCollision(Entity entity) {
    try {
      this.collisionLock.writeLock().lock();
      this.collision.add(entity);
    } finally {
      this.collisionLock.writeLock().unlock();
    }
  }

  public void removeCollision(Entity entity) {
    try {
      this.collisionLock.writeLock().lock();
      this.collision.remove(entity);
    } finally {
      this.collisionLock.writeLock().unlock();
    }
  }

  public void clearCollisions() {
    try {
      this.collisionLock.writeLock().lock();
      this.collision.clear();
    } finally {
      this.collisionLock.writeLock().unlock();
    }
  }

  public void collisions(IVoidFunction1P<Stream<Entity>> stream) {
    try {
      this.collisionLock.readLock().lock();
      stream.run(this.collision.stream());
    } finally {
      this.collisionLock.readLock().unlock();
    }
  }



}
