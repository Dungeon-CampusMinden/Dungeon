package portal.laser;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.SpikyComponent;
import contrib.systems.EventScheduler;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.Hitbox;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PortalTile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.Map;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;

/** This factory can create the laser that interact with the LaserCube and LaserReceiver. */
public class LaserFactory {

  private static final SimpleIPath LASER = new SimpleIPath("portal/laser");
  private static final SimpleIPath EMITTER_ACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_active.png");
  private static final SimpleIPath EMITTER_INACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_inactive.png");

  private final Point from;
  private final Direction direction;

  /**
   * Provides the LaserFactory with the starting point and direction of the laser.
   *
   * @param from starting point of the laser.
   * @param direction direction the laser is facing.
   */
  public LaserFactory(Point from, Direction direction) {
    this.from = from;
    this.direction = direction;
  }

  /**
   * Creates the laser, either active or not active.
   *
   * @param active true if the laser should be active, otherwise false.
   * @return the laser entity.
   */
  public Entity create(boolean active) {
    Entity emitter = createEmitter(from, direction);
    LaserComponent comp = new LaserComponent(direction);
    emitter.add(comp);
    Game.add(emitter);
    if (active) {
      activate(emitter);
    }
    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend = comp::extend;
    pec.onTrim =
        (e) -> {
          comp.trim();
        };
    emitter.add(pec);
    return emitter;
  }

  /**
   * Creates the Laser with the given starting point, direction and active state.
   *
   * @param from starting point of the laser.
   * @param direction direction the laser is facing.
   * @param active true if the laser should be active, otherwise false.
   * @return the laser entity.
   */
  public static Entity createLaser(Point from, Direction direction, boolean active) {
    return new LaserFactory(from, direction).create(active);
  }

  /**
   * Sets the given emitter to active so that it extends a laser out of it.
   *
   * @param emitter emitter that should be activated.
   */
  public static void activate(Entity emitter) {
    emitter
        .fetch(LaserComponent.class)
        .ifPresent(
            comp -> {
              if (comp.isActive()) return;

              emitter
                  .fetch(PositionComponent.class)
                  .ifPresent(
                      pc -> {
                        Direction dir = comp.getDirection();
                        Point start = pc.position().translate(dir);
                        Point end = calculateEndPoint(start, dir);
                        int totalPoints = calculateNumberOfPoints(start, end);
                        comp.getSegments().add(emitter);

                        for (int i = 0; i < totalPoints; i++) {
                          Entity segment = createSegment(start, end, totalPoints, i, dir);
                          comp.getSegments().add(segment);
                          Game.add(segment);
                        }

                        updateEmitterVisual(emitter, true, dir);
                        configureEmitterHitbox(emitter, totalPoints, dir);
                        comp.setActive(true);
                      });
            });
  }

  /**
   * Deactivates the given emitter so that it retracts its laser.
   *
   * @param emitter emitter that should be deactivated.
   */
  public static void deactivate(Entity emitter) {
    emitter
        .fetch(LaserComponent.class)
        .ifPresent(
            comp -> {
              if (!comp.isActive()) return;

              comp.getSegments()
                  .forEach(
                      seg -> {
                        if (seg.name().startsWith("laserEmitter")) {
                          if (seg.name().equals("laserEmitter")) {
                            removeEmitterHitbox(seg);
                            EventScheduler.scheduleAction(
                                () -> {
                                  Game.remove(seg);
                                },
                                100);
                          } else {
                            removeEmitterHitbox(seg);
                          }
                        } else {
                          Game.remove(seg);
                        }
                      });
              comp.getSegments().clear();

              removeEmitterHitbox(emitter);
              updateEmitterVisual(emitter, false, comp.getDirection());
              comp.setActive(false);
            });
  }

  /**
   * Creates an emitter entity.
   *
   * @param position position of the emitter.
   * @param direction direction the emitter is facing.
   * @return the emitter entity.
   */
  static Entity createEmitter(Point position, Direction direction) {
    Entity emitter = new Entity("laserEmitter");
    PositionComponent pc = new PositionComponent(position);
    pc.rotation(rotationFor(direction));
    emitter.add(pc);
    emitter.add(new PortalIgnoreComponent());
    DrawComponent dc = new DrawComponent(EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
    return emitter;
  }

  /**
   * Creates a visual segment for the laser when extended.
   *
   * @param from position where the laser starts.
   * @param to position where the laser ends.
   * @param totalPoints how long the laser is.
   * @param index which segment this is.
   * @param dir in which direction the laser is extending.
   * @return the segment entity.
   */
  static Entity createSegment(Point from, Point to, int totalPoints, int index, Direction dir) {
    int denom = Math.max(totalPoints - 1, 1);
    float x = from.x() + index * (to.x() - from.x()) / denom;
    float y = from.y() + index * (to.y() - from.y()) / denom;
    Point p = new Point(x, y);

    Entity segment = new Entity("laserSegment");
    PositionComponent pc = new PositionComponent(p);
    segment.add(pc);

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(LASER);
    State idle;
    if (dir == Direction.LEFT || dir == Direction.RIGHT) {
      idle = State.fromMap(animationMap, "horizontal");
    } else {
      idle = State.fromMap(animationMap, "vertical");
    }

    StateMachine sm = new StateMachine(List.of(idle));
    DrawComponent dc = new DrawComponent(sm);
    segment.add(dc);

    return segment;
  }

  /**
   * Updates an emitters visual and name to active or inactive.
   *
   * @param emitter the emitter entity that is getting updated.
   * @param on true if the laser is active, otherwise false.
   * @param direction direction the emitter is facing.
   */
  static void updateEmitterVisual(Entity emitter, boolean on, Direction direction) {
    DrawComponent dc = new DrawComponent(on ? EMITTER_ACTIVE : EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
    emitter.name(on ? "laserEmitterActive" : "laserEmitterInactive");
    emitter.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
  }

  /**
   * Sets the hitbox of the CollideComponent so it fits the extended laser.
   *
   * @param emitter the emitter entity that is getting updated.
   * @param totalPoints how many tiles the laser is covering.
   * @param dir direction the laser is extending to.
   */
  static void configureEmitterHitbox(Entity emitter, int totalPoints, Direction dir) {
    float hitboxX = 1f;
    float hitboxY = 1f;
    float offsetX = 0.375f;
    float offsetY = 0.375f;

    switch (dir) {
      case LEFT -> {
        hitboxX = totalPoints + 1;
        hitboxY = 0.25f;
        offsetX = (-totalPoints);
      }
      case RIGHT -> {
        hitboxX = totalPoints + 1;
        hitboxY = 0.25f;
        offsetX = 0;
      }
      case UP -> {
        hitboxX = 0.25f;
        hitboxY = totalPoints + 1;
        offsetY = 0;
      }
      case DOWN -> {
        hitboxX = 0.25f;
        hitboxY = totalPoints + 1;
        offsetY = (-totalPoints);
      }
      default -> {}
    }
    // To allow collision with the laser cube and receiver but it still doesnt get moved if you run into the laser
    emitter.remove(VelocityComponent.class);
    VelocityComponent velocityComponent = new VelocityComponent(0.0000000001f);
    velocityComponent.mass(9999);
    emitter.add(velocityComponent);
    emitter.add(new SpikyComponent(9999, DamageType.PHYSICAL, 10));
    Hitbox newCollider = new Hitbox(Vector2.of(hitboxX, hitboxY), Vector2.of(offsetX, offsetY));
    emitter
        .fetch(CollideComponent.class)
        .ifPresentOrElse(
            cc -> {
              cc.collider(newCollider);
              PositionSync.syncPosition(emitter);
            },
            () -> {
              CollideComponent cc =
                  new CollideComponent(
                    CollideComponent.DEFAULT_COLLIDER, (you, other, collisionDir) -> {});
              cc.collider(newCollider);
              emitter.add(cc);
            });

    emitter.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(false));
  }

  /**
   * Sets the hitbox to 0,0 when the laser is deactivated. Set to 0,0 so that collideLeave is still
   * getting triggered but nothing else can collide with it, effectively removing it.
   *
   * @param emitter the emitter entity which hitbox is getting removed.
   */
  static void removeEmitterHitbox(Entity emitter) {
    emitter
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.collider(new Hitbox(Vector2.ZERO, Vector2.ZERO));
            });
    PositionSync.syncPosition(emitter);
  }

  /**
   * Helper method that transforms a direction to its given degree.
   *
   * @param d the direction that is getting converted.
   * @return the resulting degree.
   */
  static float rotationFor(Direction d) {
    return switch (d) {
      case UP -> 0f;
      case DOWN -> 180f;
      case LEFT -> 90f;
      case RIGHT -> -90f;
      default -> 0f;
    };
  }

  /**
   * Helper method to figure out how long the laser is.
   *
   * @param from starting point of the laser.
   * @param to end point of the laser.
   * @return how long the laser is.
   */
  static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /**
   * Helper method to figure out the end position of the laser.
   *
   * @param from starting position of the laser.
   * @param beamDirection direction of the laser.
   * @return the end position of the laser.
   */
  static Point calculateEndPoint(Point from, Direction beamDirection) {
    Point lastPoint = from;
    Point currentPoint = from;
    Tile currentTile = Game.tileAt(from).orElse(null);
    while (currentTile != null
        && !(currentTile instanceof WallTile)
        && !(currentTile instanceof PortalTile)
        && !Game.entityAtTile(currentTile)
            .anyMatch(entity -> entity.name().startsWith("laserCube"))) {
      lastPoint = currentPoint;
      currentPoint = currentPoint.translate(beamDirection);
      currentTile = Game.tileAt(currentPoint).orElse(null);
    }
    return lastPoint;
  }

  /**
   * Extends the laser on a new surface.
   *
   * @param direction direction of the laser.
   * @param from starting point of the laser.
   * @param laserEntities list of laser components, needed for deleting.
   * @param pec needed so the new laser also retracts when the original laser retracts.
   * @param comp needed so the new laser can also extend and retract.
   */
  public static void extendLaser(
      Direction direction,
      Point from,
      List<Entity> laserEntities,
      PortalExtendComponent pec,
      LaserComponent comp) {
    Point startintPoint = from.translate(direction);
    Point end = calculateEndPoint(startintPoint, direction);
    int totalPoints = calculateNumberOfPoints(startintPoint, end);

    Entity newEmitter = createEmitter(from, direction);
    newEmitter.add(comp);
    newEmitter.add(pec);
    newEmitter.remove(DrawComponent.class);
    laserEntities.add(newEmitter);

    for (int i = 0; i < totalPoints; i++) {
      Entity segment = createSegment(startintPoint, end, totalPoints, i, direction);
      laserEntities.add(segment);
      Game.add(segment);
    }

    configureEmitterHitbox(newEmitter, totalPoints, direction);
    comp.setActive(true);

    Game.add(newEmitter);
  }

  /**
   * Removes all entities after the first extension, including the emitter of the extension.
   *
   * @param entities list of all entities of the laser.
   */
  public static void trimAfterFirstEmitter(List<Entity> entities) {
    int firstEmitterIndex = -1;
    boolean skippedFirstEmitter = false;
    for (int i = 0; i < entities.size(); i++) {
      if ("laserEmitter".equals(entities.get(i).name())) {
        if (entities.get(i).fetch(LaserComponent.class).get().isThroughCube()
            && !skippedFirstEmitter) {
          skippedFirstEmitter = true;
          continue;
        }
        firstEmitterIndex = i;
        break;
      }
    }
    if (firstEmitterIndex != -1 && firstEmitterIndex + 1 < entities.size()) {
      List<Entity> toRemove = entities.subList(firstEmitterIndex, entities.size());
      toRemove.forEach(
          seg -> {
            if (seg.name().startsWith("laserEmitter")) {
              removeEmitterHitbox(seg);
              EventScheduler.scheduleAction(
                  () -> {
                    Game.remove(seg);
                  },
                  100);
            } else {
              Game.remove(seg);
            }
          });
      toRemove.clear();
    }
  }
}
