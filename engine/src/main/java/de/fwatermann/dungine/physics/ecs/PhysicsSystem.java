package de.fwatermann.dungine.physics.ecs;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.graphics.simple.Cuboid;
import de.fwatermann.dungine.graphics.simple.Points;
import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.physics.colliders.Collision;
import de.fwatermann.dungine.physics.colliders.CollisionResult;
import de.fwatermann.dungine.utils.pair.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * The `PhysicsSystem` class is responsible for managing the physics simulation in the ECS
 * framework. It handles gravity, collision detection, and resolution, and updates the positions of
 * entities based on their velocities.
 */
public class PhysicsSystem extends System<PhysicsSystem> {

  /** The default gravity constant. */
  public static final float DEFAULT_GRAVITY_CONSTANT = 9.81f;

  /** The default sleep threshold. */
  public static final float DEFAULT_SLEEP_THRESHOLD = 0.01f;

  /** The default size of a physics chunk. */
  public static final int DEFAULT_PHYSIC_CHUNK_SIZE = 1;

  /** Logger for the PhysicsSystem class. */
  private static final Logger LOGGER = LogManager.getLogger(PhysicsSystem.class);

  /** The size of the physics chunks. */
  private final Vector3i physicChunkSize;

  /** Map of chunks containing entities. */
  private final Map<Integer, Map<Integer, Map<Integer, List<Entity>>>> chunks = new HashMap<>();

  /** Map of entities and their corresponding chunks. */
  private final Map<Entity, Pair<Vector3i, Vector3i>> entityChunks = new HashMap<>();

  /** The gravity constant. */
  private float gravityConstant = DEFAULT_GRAVITY_CONSTANT;

  /** The sleep threshold. */
  private float sleepThreshold = DEFAULT_SLEEP_THRESHOLD;

  /** The time of the last execution. */
  private long lastExecution = java.lang.System.nanoTime();

  /** The delta time of the last update. */
  private float lastDeltaTime = 0.0f;

  /** The number of updates in the last frame. */
  private int lastUpdates = 0;

  /** The number of updates in the current frame. */
  private int lUc = 0;

  /** Points object for debugging. */
  private static Points debugPoints;

  /**
   * Returns the debug points object, creating it if necessary.
   *
   * @return the debug points object
   */
  private static Points debugPoints() {
    if (debugPoints == null) {
      debugPoints = new Points(0xFFFFFFFF);
      debugPoints.pointSize(5.0f);
      PhysicsDebugSystem.addPoints(debugPoints);
    }
    return debugPoints;
  }

  /**
   * Constructs a new PhysicsSystem with the specified gravity constant and physics chunk size.
   *
   * @param gravityConstant the gravity constant
   * @param physicChunkSize the size of the physics chunks
   */
  public PhysicsSystem(float gravityConstant, Vector3i physicChunkSize) {
    super(0, false, RigidBodyComponent.class);
    this.gravityConstant = gravityConstant;
    this.physicChunkSize = new Vector3i(physicChunkSize);
  }

  /**
   * Constructs a new PhysicsSystem with the specified physics chunk size.
   *
   * @param physicChunkSize the size of the physics chunks
   */
  public PhysicsSystem(Vector3i physicChunkSize) {
    this(DEFAULT_GRAVITY_CONSTANT, physicChunkSize);
  }

  /**
   * Constructs a new PhysicsSystem with the specified gravity constant.
   *
   * @param gravityConstant the gravity constant
   */
  public PhysicsSystem(float gravityConstant) {
    this(gravityConstant, new Vector3i(DEFAULT_PHYSIC_CHUNK_SIZE));
  }

  /** Constructs a new PhysicsSystem with default settings. */
  public PhysicsSystem() {
    this(DEFAULT_GRAVITY_CONSTANT);
  }

  /**
   * Updates the physics system.
   *
   * @param ecs the ECS instance
   */
  @Override
  public void update(ECS ecs) {
    float deltaTime = (java.lang.System.nanoTime() - this.lastExecution) / 1_000_000_000.0f;
    this.lastExecution = java.lang.System.nanoTime();
    this.lastDeltaTime = deltaTime;
    this.lUc = 0;

    debugPoints().clear();

    ecs.forEachEntity(
        entity -> {
          Optional<RigidBodyComponent> opt = entity.component(RigidBodyComponent.class);
          if (opt.isEmpty()) return;
          RigidBodyComponent rbc = opt.get();
          if (rbc.sleeping() || rbc.kinematic()) return; // Skip sleeping/kinematic entities.
          this.lUc++;

          if (rbc.gravity()) {
            rbc.applyForce(
                0.0f,
                -this.gravityConstant * deltaTime,
                0.0f,
                RigidBodyComponent.ForceMode.ACCELERATION,
                false);
          }

          Vector3f acceleration = rbc.force().div(rbc.mass());
          if (acceleration.length() > this.sleepThreshold) {
            rbc.velocity().add(acceleration);
          }
          rbc.force(0, 0, 0, false);

          // Change position of rigid bodies based on their velocity
          Vector3f oldPos = new Vector3f(entity.position());
          entity.position().add(rbc.velocity().mul(deltaTime, new Vector3f()));

          // Collision detection and resolution
          boolean collided = this.collisionCheck(entity);
          if (collided) {
            entity.position(oldPos);
            rbc.velocity().set(0);
          }

          entity
              .component(RenderableComponent.class)
              .ifPresent(
                  rc -> {
                    if (rc.renderable instanceof Cuboid cube) {
                      if (collided) {
                        cube.color(0xFF0000FF);
                      } else {
                        cube.color(0x0000FFFF);
                      }
                    }
                  });

          this.updateChunkOfEntity(entity, rbc);
        },
        RigidBodyComponent.class);
    this.lastUpdates = this.lUc;
  }

  /**
   * Handles the addition of an entity to the physics system.
   *
   * @param ecs the ECS instance
   * @param entity the entity to add
   */
  @Override
  public void onEntityAdd(ECS ecs, Entity entity) {
    Optional<RigidBodyComponent> opt = entity.component(RigidBodyComponent.class);
    if (opt.isEmpty()) return;
    RigidBodyComponent rbc = opt.get();
    entity.position().set(entity.position());

    Pair<Vector3i, Vector3i> pair = this.getMinMax(entity, rbc);
    this.getChunksBetween(pair.a(), pair.b())
        .forEach(
            c -> {
              this.getChunkEntityList(c, true).ifPresent(l -> l.add(entity));
            });
    this.entityChunks.put(entity, pair);
    LOGGER.debug("Added entity to chunk {}", this.toChunkCoordinates(entity.position()));
  }

  /**
   * Handles the removal of an entity from the physics system.
   *
   * @param ecs the ECS instance
   * @param entity the entity to remove
   */
  @Override
  public void onEntityRemove(ECS ecs, Entity entity) {
    Pair<Vector3i, Vector3i> pair = this.entityChunks.get(entity);
    if (pair != null) {
      this.getChunksBetween(pair.a(), pair.b())
          .forEach(
              c -> {
                this.getChunkEntityList(c, false)
                    .ifPresent(
                        l -> {
                          l.remove(entity);
                          l.forEach(
                              e -> {
                                Optional<RigidBodyComponent> optRbc =
                                    e.component(RigidBodyComponent.class);
                                if (optRbc.isEmpty()) return;
                                RigidBodyComponent rbc2 = optRbc.get();
                                rbc2.sleeping(false);
                              });
                        });
                this.checkDeletion(c);
              });
    }
    LOGGER.debug("Removed entity from chunk {}", this.toChunkCoordinates(entity.position()));
  }

  /**
   * Checks for collisions between the specified entity and other entities.
   *
   * @param entity the entity to check for collisions
   * @return true if a collision occurred, false otherwise
   */
  private boolean collisionCheck(Entity entity) {

    Optional<RigidBodyComponent> optRbc = entity.component(RigidBodyComponent.class);
    if (optRbc.isEmpty()) return false;
    RigidBodyComponent rbc = optRbc.get();

    Pair<Vector3i, Vector3i> minMax = this.getMinMax(entity, rbc);
    List<Vector3i> chunks = this.getChunksBetween(minMax.a(), minMax.b()).toList();

    boolean collided = false;

    for (Vector3i chunk : chunks) {
      Optional<List<Entity>> optEntities = this.getChunkEntityList(chunk, false);
      if (optEntities.isEmpty()) continue;
      List<Entity> entities = optEntities.get();
      for (Entity entity2 : entities) {
        if (entity == entity2) continue;
        Optional<RigidBodyComponent> optRbc2 = entity2.component(RigidBodyComponent.class);
        if (optRbc2.isEmpty()) continue;
        RigidBodyComponent rbc2 = optRbc2.get();

        Vector3f v1 = rbc.velocity();
        Vector3f v2 = rbc2.velocity();

        for (Collider c : rbc.colliders()) {
          List<Collision> collisions = new ArrayList<>();
          rbc2.colliders()
              .forEach(
                  c2 -> {
                    CollisionResult result = c.collide(c2);
                    if (result.collided()) {
                      collisions.addAll(result.collisions());
                    }
                  });
          if (collisions.isEmpty()) continue;
          collided = true;

          if (PhysicsDebugSystem.isEnabled(PhysicsDebugSystem.OPTION_CONTACT_POINTS)) {
            collisions.forEach(
                collision -> {
                  collision.collisionPoints().forEach(p -> debugPoints().addPoint(p, 0xFF00FFFF));
                });
          }
        }
      }
    }

    return collided;
  }

  /**
   * Returns the minimum and maximum chunk coordinates for the specified entity and rigid body
   * component.
   *
   * @param entity the entity
   * @param rbc the rigid body component
   * @return a pair of minimum and maximum chunk coordinates
   */
  private Pair<Vector3i, Vector3i> getMinMax(Entity entity, RigidBodyComponent rbc) {
    Vector3f min = new Vector3f(Float.MAX_VALUE);
    Vector3f max = new Vector3f(-Float.MAX_VALUE);
    rbc.colliders()
        .forEach(
            c -> {
              min.min(c.min());
              max.max(c.max());
            });
    Vector3i minChunk = this.toChunkCoordinates(min);
    Vector3i maxChunk = this.toChunkCoordinates(max);
    return new Pair<>(minChunk, maxChunk);
  }

  /**
   * Returns a stream of chunk coordinates between the specified minimum and maximum coordinates.
   *
   * @param min the minimum chunk coordinates
   * @param max the maximum chunk coordinates
   * @return a stream of chunk coordinates
   */
  private Stream<Vector3i> getChunksBetween(Vector3i min, Vector3i max) {
    List<Vector3i> chunks = new ArrayList<>();
    for (int x = min.x; x <= max.x; x++) {
      for (int y = min.y; y <= max.y; y++) {
        for (int z = min.z; z <= max.z; z++) {
          chunks.add(new Vector3i(x, y, z));
        }
      }
    }
    return chunks.stream();
  }

  /**
   * Updates the chunk of the specified entity.
   *
   * @param entity the entity
   * @param rbc the rigid body component
   */
  private void updateChunkOfEntity(Entity entity, RigidBodyComponent rbc) {
    Pair<Vector3i, Vector3i> old = this.entityChunks.get(entity);
    Pair<Vector3i, Vector3i> neu = this.getMinMax(entity, rbc);
    if (old != null && old.equals(neu)) return;
    if (old != null) {
      this.getChunksBetween(old.a(), old.b())
          .forEach(
              c -> {
                this.getChunkEntityList(c, false).ifPresent(l -> l.remove(entity));
                this.checkDeletion(c);
              });
    }
    this.getChunksBetween(neu.a(), neu.b())
        .forEach(
            c -> {
              this.getChunkEntityList(c, true).ifPresent(l -> l.add(entity));
            });
    this.entityChunks.put(entity, neu);
  }

  /**
   * Checks if a chunk should be deleted and removes it if necessary.
   *
   * @param chunk the chunk to check
   */
  private void checkDeletion(Vector3i chunk) {
    Map<Integer, Map<Integer, List<Entity>>> mapX = this.chunks.get(chunk.x);
    if (mapX == null) return;
    Map<Integer, List<Entity>> mapY = mapX.get(chunk.y);
    if (mapY == null) return;
    List<Entity> list = mapY.get(chunk.z);
    if (list == null) return;
    if (list.isEmpty()) {
      mapY.remove(chunk.z);
      if (mapY.isEmpty()) {
        mapX.remove(chunk.y);
        if (mapX.isEmpty()) {
          this.chunks.remove(chunk.x);
        }
      }
    }
  }

  /**
   * Returns the list of entities in the specified chunk, creating it if necessary.
   *
   * @param chunk the chunk coordinates
   * @param create whether to create the list if it does not exist
   * @return an optional containing the list of entities
   */
  private Optional<List<Entity>> getChunkEntityList(Vector3i chunk, boolean create) {
    Map<Integer, Map<Integer, List<Entity>>> mapX = this.chunks.get(chunk.x);
    if (mapX == null) {
      if (!create) return Optional.empty();
      mapX = new HashMap<>();
      this.chunks.put(chunk.x, mapX);
    }
    Map<Integer, List<Entity>> mapY = mapX.get(chunk.y);
    if (mapY == null) {
      if (!create) return Optional.empty();
      mapY = new HashMap<>();
      mapX.put(chunk.y, mapY);
    }
    List<Entity> list = mapY.get(chunk.z);
    if (list == null) {
      if (!create) return Optional.empty();
      list = new ArrayList<>();
      mapY.put(chunk.z, list);
    }
    return Optional.of(list);
  }

  /**
   * Converts world coordinates to chunk coordinates.
   *
   * @param worldPos the world coordinates
   * @return the chunk coordinates
   */
  private Vector3i toChunkCoordinates(Vector3f worldPos) {
    return new Vector3i(
        (int) Math.floor(worldPos.x / this.physicChunkSize.x),
        (int) Math.floor(worldPos.y / this.physicChunkSize.y),
        (int) Math.floor(worldPos.z / this.physicChunkSize.z));
  }

  /**
   * Returns the gravity constant.
   *
   * @return the gravity constant
   */
  public float gravityConstant() {
    return this.gravityConstant;
  }

  /**
   * Sets the gravity constant.
   *
   * @param gravityConstant the new gravity constant
   * @return this PhysicsSystem instance for method chaining
   */
  public PhysicsSystem gravityConstant(float gravityConstant) {
    this.gravityConstant = gravityConstant;
    return this;
  }

  /**
   * Returns the sleep threshold.
   *
   * @return the sleep threshold
   */
  public float sleepThreshold() {
    return this.sleepThreshold;
  }

  /**
   * Sets the sleep threshold.
   *
   * @param sleepThreshold the new sleep threshold
   * @return this PhysicsSystem instance for method chaining
   */
  public PhysicsSystem sleepThreshold(float sleepThreshold) {
    this.sleepThreshold = sleepThreshold;
    return this;
  }

  /**
   * Returns the delta time of the last update.
   *
   * @return the delta time of the last update
   */
  public float lastDeltaTime() {
    return this.lastDeltaTime;
  }

  /**
   * Returns the number of updates in the last frame.
   *
   * @return the number of updates in the last frame
   */
  public int lastUpdates() {
    return this.lastUpdates;
  }
}
