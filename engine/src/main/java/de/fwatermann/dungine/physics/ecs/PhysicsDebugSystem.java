package de.fwatermann.dungine.physics.ecs;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.simple.Lines;
import de.fwatermann.dungine.graphics.simple.Points;
import de.fwatermann.dungine.graphics.simple.WireframeBox;
import de.fwatermann.dungine.physics.colliders.PolyhedronCollider;
import de.fwatermann.dungine.utils.pair.IntPair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A system that is used to debug physics. This system is used to display debug information about
 * physics components.
 */
public class PhysicsDebugSystem extends System<PhysicsDebugSystem> {

  private static final Logger LOGGER = LogManager.getLogger(PhysicsDebugSystem.class);

  private static PhysicsDebugSystem instance;

  public static PhysicsDebugSystem instance() {
    if(instance == null) {
      instance = new PhysicsDebugSystem();
    }
    return instance;
  }

  public static final int OPTION_ALL = 0xFFFFFFFF;
  public static final int OPTION_ENTITY_POSITION = 0x00000001;
  public static final int OPTION_BOUNDING_BOX = 0x00000002;
  public static final int OPTION_VELOCITY = 0x00000004;
  public static final int OPTION_FORCE = 0x00000008;
  public static final int OPTION_COLLIDERS = 0x00000010;
  public static final int OPTION_CONTACT_POINTS = 0x00000020;

  private static final ReentrantReadWriteLock linesLock = new ReentrantReadWriteLock();
  private static final ReentrantReadWriteLock pointsLock = new ReentrantReadWriteLock();
  private static final Set<Lines> linesList = new HashSet<>();
  private static final Set<Points> pointsList = new HashSet<>();

  private static WireframeBox boundingBox;
  private static Camera<?> camera;
  private static final Lines lines = new Lines(0xFFFFFFFF);
  private static final Points points = new Points(0xFFFFFFFF);
  private static final Map<Integer, Integer> optionColors = new HashMap<>();
  private static int options = 0x00;

  static {
    linesList.add(lines);
    pointsList.add(points);

    optionColors.put(OPTION_ENTITY_POSITION, 0xFF00FFFF);
    optionColors.put(OPTION_BOUNDING_BOX, 0x0000FFFF);
    optionColors.put(OPTION_VELOCITY, 0xFF8000FF);
    optionColors.put(OPTION_FORCE, 0xFF0000FF);
    optionColors.put(OPTION_COLLIDERS, 0x00FF00FF);
  }


  private PhysicsDebugSystem() {
    super(0, true);
  }

  @Override
  public void update(ECS ecs) {

    lines.clear();
    points.clear();

    if(camera == null) return;

    ecs.forEachEntity(
        entity -> {
          Optional<RigidBodyComponent> optRBC = entity.component(RigidBodyComponent.class);

          if (isEnabled(OPTION_ENTITY_POSITION)) {
            points.addPoint(new Vector3f(entity.position()), color(OPTION_ENTITY_POSITION));
          }

          if (isEnabled(OPTION_BOUNDING_BOX)) {
            initBoundingBox(entity);
            boundingBox.render(camera);
          }

          if (isEnabled(OPTION_VELOCITY)) {
            optRBC.ifPresent(
                rbc -> {
                  Vector3f center = rbc.getCenterOfMass();
                  lines.addLine(
                      center, center.add(rbc.velocity(), new Vector3f()), color(OPTION_VELOCITY));
                });
          }

          if (isEnabled(OPTION_FORCE)) {
            optRBC.ifPresent(
                rbc -> {
                  Vector3f center = rbc.getCenterOfMass();
                  lines.addLine(
                      center, center.add(rbc.force(), new Vector3f()), color(OPTION_FORCE));
                });
          }

          if (isEnabled(OPTION_COLLIDERS)) {
            int color = color(OPTION_COLLIDERS);
            optRBC.ifPresent(
                rbc -> {
                  rbc.colliders().stream()
                      .filter(c -> c instanceof PolyhedronCollider<?>)
                      .forEach(
                          c -> {
                            PolyhedronCollider<?> pc = (PolyhedronCollider<?>) c;
                            Vector3f[] vertices = pc.vertices();
                            IntPair[] edges = pc.edges();
                            for (IntPair edge : edges) {
                              lines.addLine(
                                  vertices[edge.a()], vertices[edge.b()], color);
                            }
                          });
                });
          }
        });

    try {
      linesLock.readLock().lock();
      linesList.forEach(l -> l.render(camera));
    } finally {
      linesLock.readLock().unlock();
    }

    try {
      pointsLock.readLock().lock();
      pointsList.forEach(p -> p.render(camera));
    } finally {
      pointsLock.readLock().unlock();
    }
  }

  private static void initBoundingBox(Entity entity) {
    if (boundingBox == null) {
      boundingBox = new WireframeBox().color(0x0000FFFF);
    }
    Optional<RigidBodyComponent> rbcOpt = entity.component(RigidBodyComponent.class);
    if (rbcOpt.isEmpty()) {
      boundingBox.position(entity.position()).rotation(entity.rotation()).scale(entity.size());
    } else {
      RigidBodyComponent rbc = rbcOpt.get();
      Vector3f min = new Vector3f(Float.MAX_VALUE);
      Vector3f max = new Vector3f(-Float.MAX_VALUE);
      rbc.colliders()
          .forEach(
              collider -> {
                min.min(collider.min());
                max.max(collider.max());
              });
      boundingBox.position(min).scaling(max.sub(min)).rotation(new Quaternionf());
      boundingBox.color(color(OPTION_BOUNDING_BOX));
    }
  }

  public static Camera<?> camera() {
    return camera;
  }

  public static void camera(Camera<?> camera) {
    PhysicsDebugSystem.camera = camera;
  }

  public static void addLines(Lines lines) {
    try {
      linesLock.writeLock().lock();
      linesList.add(lines);
    } finally {
      linesLock.writeLock().unlock();
    }
  }

  public static void removeLines(Lines lines) {
    try {
      linesLock.writeLock().lock();
      linesList.remove(lines);
    } finally {
      linesLock.writeLock().unlock();
    }
  }

  public static void addPoints(Points points) {
    try {
      pointsLock.writeLock().lock();
      pointsList.add(points);
    } finally {
      pointsLock.writeLock().unlock();
    }
  }

  public static void removePoints(Points points) {
    try {
      pointsLock.writeLock().lock();
      pointsList.remove(points);
    } finally {
      pointsLock.writeLock().unlock();
    }
  }

  public static void enable(int option) {
    options |= option;
  }

  public static void disable(int option) {
    options &= ~option;
  }

  public static boolean isEnabled(int option) {
    return (options & option) != 0;
  }

  public static void color(int option, int color) {
    optionColors.put(option, color);
  }

  public static int color(int option) {
    return optionColors.get(option);
  }

}
