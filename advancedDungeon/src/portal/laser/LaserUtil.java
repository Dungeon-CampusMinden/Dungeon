package portal.laser;

import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
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
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.Set;
import portal.portals.components.PortalExtendComponent;

/** Util class for everything laser related. */
public class LaserUtil {

  private static final SimpleIPath EMITTER_ACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_active.png");
  private static final SimpleIPath EMITTER_INACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_inactive.png");

  /**
   * Sets the given emitter to active so it extends its laser.
   *
   * @param emitter emitter that should be activated.
   */
  public static void activate(Entity emitter) {
    emitter
        .fetch(LaserComponent.class)
        .ifPresent(
            lc -> {
              emitter
                  .fetch(PositionComponent.class)
                  .ifPresent(
                      pc -> {
                        Direction dir = pc.viewDirection();
                        Point start = pc.position().translate(dir);
                        Point end = calculateEndPoint(start, dir);
                        int totalPoints = calculateNumberOfPoints(start, end);

                        for (int i = 0; i < totalPoints; i++) {
                          Entity segment =
                              LaserFactory.createSegment(start, end, totalPoints, i, dir);
                          segment.add(lc);
                          Game.add(segment);
                        }

                        updateEmitterVisual(emitter, true);
                        configureEmitterHitbox(emitter, totalPoints, dir);
                        lc.setActive(true);
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
            lc -> {
              List<Entity> listOfRelevantEntities =
                  Game.levelEntities(Set.of(LaserComponent.class))
                      .filter(entity -> emitter.fetch(LaserComponent.class).get().equals(lc))
                      .toList();
              for (Entity entity : listOfRelevantEntities) {
                if (emitter.equals(entity)) {
                  updateEmitterVisual(emitter, false);
                  removeEmitterHitbox(emitter);
                } else {
                  Game.remove(entity);
                }
              }
              lc.setActive(false);
            });
  }

  /**
   * Extends the laser on a new surface.
   *
   * @param direction direction of the laser.
   * @param from starting point of the laser.
   * @param pec needed so the new laser also retracts when the original laser retracts.
   * @param comp needed so the new laser can also extend and retract.
   */
  public static void extendLaser(
      Direction direction, Point from, PortalExtendComponent pec, LaserComponent comp) {
    Point startintPoint = from.translate(direction);
    Point end = calculateEndPoint(startintPoint, direction);
    int totalPoints = calculateNumberOfPoints(startintPoint, end);

    Entity newEmitter = LaserFactory.createEmitter(comp.isActive(), from, direction);
    newEmitter.add(comp);
    newEmitter.add(pec);
    newEmitter.remove(DrawComponent.class);

    for (int i = 0; i < totalPoints; i++) {
      Entity segment = LaserFactory.createSegment(startintPoint, end, totalPoints, i, direction);
      segment.add(comp);
      Game.add(segment);
    }

    configureEmitterHitbox(newEmitter, totalPoints, direction);
    Game.add(newEmitter);
  }

  /**
   * Logically trims the laser by calling the deactivate and then the active methods so it acts as
   * "trimming".
   *
   * @param emitter the original emitter of the laser
   */
  public static void trimLaser(Entity emitter) {
    deactivate(emitter);
    activate(emitter);
  }

  /**
   * Helper method to figure out the end position of the laser.
   *
   * @param from starting position of the laser.
   * @param beamDirection direction of the laser.
   * @return the end position of the laser.
   */
  private static Point calculateEndPoint(Point from, Direction beamDirection) {
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
   * Helper method to figure out how long the laser is.
   *
   * @param from starting point of the laser.
   * @param to end point of the laser.
   * @return how long the laser is.
   */
  private static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /**
   * Sets the hitbox of the CollideComponent so it fits the extended laser.
   *
   * @param emitter the emitter entity that is getting updated.
   * @param totalPoints how many tiles the laser is covering.
   * @param dir direction the laser is extending to.
   */
  private static void configureEmitterHitbox(Entity emitter, int totalPoints, Direction dir) {
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
    // To allow collision with the laser cube and receiver but it still doesnt get moved if you run
    // into the laser
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
              CollideComponent cc = new CollideComponent();
              cc.collider(newCollider);
              emitter.add(cc);
            });

    emitter.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(false));
  }

  /**
   * Updates an emitters visual and name to active or inactive.
   *
   * @param emitter the emitter entity that is getting updated.
   * @param on true if the laser is active, otherwise false.
   */
  private static void updateEmitterVisual(Entity emitter, boolean on) {
    DrawComponent dc = new DrawComponent(on ? EMITTER_ACTIVE : EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
  }

  /**
   * Sets the hitbox to 0,0 when the laser is deactivated. Set to 0,0 so that collideLeave is still
   * getting triggered but nothing else can collide with it, effectively removing it.
   *
   * @param emitter the emitter entity which hitbox is getting removed.
   */
  private static void removeEmitterHitbox(Entity emitter) {
    emitter
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.collider(new Hitbox(Vector2.ZERO, Vector2.ZERO));
              PositionSync.syncPosition(emitter);
            });
  }
}
