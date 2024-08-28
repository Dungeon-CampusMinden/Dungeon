package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.RigidBodyComponent;
import de.fwatermann.dungine.physics.Collider;
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

public class PhysicsSystem extends System<PhysicsSystem> {

  public static final float DEFAULT_GRAVITY_CONSTANT = 9.81f;
  private static final Logger LOGGER = LogManager.getLogger(PhysicsSystem.class);

  private final Vector3i physicChunkSize;
  private final Map<Integer, Map<Integer, Map<Integer, List<Entity>>>> chunks = new HashMap<>();
  private final Map<Entity, VectorPair> entityChunks = new HashMap<>();

  private float gravityConstant;
  private long lastExecution = java.lang.System.currentTimeMillis();
  private float lastDeltaTime = 0.0f;
  private int lastUpdates = 0;
  private int lUc = 0;

  public PhysicsSystem(float gravityConstant, Vector3i physicChunkSize) {
    super(0, false, RigidBodyComponent.class);
    this.gravityConstant = gravityConstant;
    this.physicChunkSize = new Vector3i(physicChunkSize);
  }

  public PhysicsSystem(float gravityConstant) {
    this(gravityConstant, new Vector3i(10, 10, 10));
  }

  public PhysicsSystem() {
    this(DEFAULT_GRAVITY_CONSTANT);
  }

  @Override
  public void update(ECS ecs) {
    float deltaTime = (java.lang.System.currentTimeMillis() - this.lastExecution) / 1000.0f;
    this.lastDeltaTime = deltaTime;
    this.lUc = 0;
    ecs.entities(
        (s) ->
            s.forEach(
                e -> {
                  Optional<RigidBodyComponent> opt = e.component(RigidBodyComponent.class);
                  if (opt.isEmpty()) return;
                  RigidBodyComponent rb = opt.get();
                  if(rb.sleeping() || rb.kinematic()) return; //Skip sleeping entities.
                  this.lUc ++;

                  if(rb.gravity()) {
                    rb.applyForce(
                      0.0f,
                      -this.gravityConstant * deltaTime,
                      0.0f,
                      RigidBodyComponent.ForceMode.ACCELERATION);
                  }

                  rb.velocity().add(rb.force().div(rb.mass()));
                  rb.force(new Vector3f(0.0f));

                  // Change position of rigid bodies based on their velocity
                  Vector3f oldPos = new Vector3f(e.position());
                  e.position().add(rb.velocity().mul(deltaTime, new Vector3f()));

                  // Collision detection and resolution
                  this.collisionCheck(e, oldPos);

                  this.updateChunkOfEntity(e, oldPos);

                  if(oldPos.distance(e.position()) < 0.01f) {
                    rb.velocity(new Vector3f());
                    rb.sleepCounter ++;
                    if(rb.sleepCounter > 10) {
                      rb.sleeping(true);
                    }
                  } else {
                    rb.sleepCounter = 0;
                  }
                }),
        RigidBodyComponent.class);
    this.lastExecution = java.lang.System.currentTimeMillis();
    this.lastUpdates = this.lUc;
  }

  @Override
  public void onEntityAdd(ECS ecs, Entity entity) {
    VectorPair pair = this.getMinMax(entity);
    if(pair != null) {
      this.getChunksBetween(pair.min, pair.max).forEach(c -> {
        this.getChunkEntityList(c, true).ifPresent(l -> l.add(entity));
      });
      this.entityChunks.put(entity, pair);
    }

    LOGGER.debug("Added entity to chunk {}", this.toChunkCoordinates(entity.position()));
  }

  @Override
  public void onEntityRemove(ECS ecs, Entity entity) {
    VectorPair pair = this.entityChunks.get(entity);
    if(pair != null) {
      this.getChunksBetween(pair.min, pair.max).forEach(c -> {
        this.getChunkEntityList(c, false).ifPresent(l -> {
          l.remove(entity);
          l.forEach(e -> e.component(RigidBodyComponent.class).ifPresent(c2 -> c2.sleeping(false))); //wake up all entities
        });
        this.checkDeletion(c);
      });
    }
    LOGGER.debug("Removed entity from chunk {}", this.toChunkCoordinates(entity.position()));
  }

  private void collisionCheck(Entity e, Vector3f oldPos) {

    Optional<RigidBodyComponent> optRbc = e.component(RigidBodyComponent.class);
    if (optRbc.isEmpty()) return;
    RigidBodyComponent rbc = optRbc.get();

    VectorPair minMax = this.getMinMax(e);
    if(minMax == null) return;

    List<Vector3i> chunks = this.getChunksBetween(minMax.min, minMax.max).toList();

    chunks.stream().map(v -> this.getChunkEntityList(v, false)).filter(Optional::isPresent).forEach(l -> {
      l.get().forEach(e2 -> {
        if(e2 == e) return;
        Optional<RigidBodyComponent> optRbc2 = e2.component(RigidBodyComponent.class);
        if(optRbc2.isEmpty()) return;
        RigidBodyComponent rbc2 = optRbc2.get();

        rbc.colliders().forEach(c -> {
          rbc2.colliders().forEach(c2 -> {

            Collider.CollisionResult result = c.collide(c2);

            if(result.collided()) {
              e.position().set(oldPos);

              if(rbc2.kinematic()) {
                Vector3f v1 = new Vector3f(rbc.velocity());
                Vector3f norm = result.normal();
                Vector3f u1 = v1.reflect(norm, new Vector3f()).mul(rbc2.bounciness());
                rbc.velocity(u1);
                return;
              }

              float m1 = rbc.mass();
              float m2 = rbc2.mass();
              Vector3f v1 = rbc.velocity();
              Vector3f v2 = rbc2.velocity();

              Vector3f u1 = v1.mul(m1 - m2, new Vector3f()).add(v2.mul(2 * m2, new Vector3f())).div(m1 + m2);
              Vector3f u2 = v2.mul(m2 - m1, new Vector3f()).add(v1.mul(2 * m1, new Vector3f())).div(m1 + m2);

              rbc.velocity(u1);
              rbc2.velocity(u2);
            }
          });
        });
      });
    });
  }

  private VectorPair getMinMax(Entity entity) {
    Optional<RigidBodyComponent> optRbc = entity.component(RigidBodyComponent.class);
    if (optRbc.isEmpty()) return null;
    RigidBodyComponent rbc = optRbc.get();

    Vector3f min = new Vector3f(entity.position());
    Vector3f max = new Vector3f(entity.position());

    rbc.colliders()
        .forEach(
            c -> {
              min.min(c.min());
              max.max(c.max());
            });
    Vector3i minChunk = this.toChunkCoordinates(min);
    Vector3i maxChunk = this.toChunkCoordinates(max);
    return new VectorPair(minChunk, maxChunk);
  }

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

  private void updateChunkOfEntity(Entity entity, Vector3f oldPos) {
    VectorPair old = this.entityChunks.get(entity);
    VectorPair neu = this.getMinMax(entity);
    if(old != null && old.equals(neu)) return;
    if(old != null) {
      this.getChunksBetween(old.min, old.max).forEach(c -> {
        this.getChunkEntityList(c, false).ifPresent(l -> l.remove(entity));
        this.checkDeletion(c);
      });
    }
    this.getChunksBetween(neu.min, neu.max).forEach(c -> {
      this.getChunkEntityList(c, true).ifPresent(l -> l.add(entity));
    });
    this.entityChunks.put(entity, neu);
    LOGGER.debug("Updated chunk of entity {}", entity);
  }

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

  private Vector3i toChunkCoordinates(Vector3f worldPos) {
    return new Vector3i(
        (int) Math.floor(worldPos.x / this.physicChunkSize.x),
        (int) Math.floor(worldPos.y / this.physicChunkSize.y),
        (int) Math.floor(worldPos.z / this.physicChunkSize.z));
  }

  private record VectorPair(Vector3i min, Vector3i max) {
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof VectorPair vp)) return false;
      return this.min.equals(vp.min) && this.max.equals(vp.max);
    }

  }

  public float gravityConstant() {
    return this.gravityConstant;
  }

  public PhysicsSystem gravityConstant(float gravityConstant) {
    this.gravityConstant = gravityConstant;
    return this;
  }

  public float lastDeltaTime() {
    return this.lastDeltaTime;
  }

  public int lastUpdates() {
    return this.lastUpdates;
  }

}
