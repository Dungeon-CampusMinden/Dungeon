package portal.tractorBeam;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import contrib.hud.DialogUtils;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.Hitbox;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.GlasswandTile;
import core.level.elements.tile.PortalTile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import portal.PortalRegistry;
import portal.portals.components.PortalComponent;

public class TractorBeamUtil {
  private static final String BEAMFORCE =
      "Die Berechnung der Kraft des Traktorstrahls ist nicht richtig.";
  private static String beamEmitterName = "beamEmitter";

  /** Activates the TractorBeam if not already active. */
  public static void activate(Entity beam) {
    beam.fetch(TractorBeamComponent.class)
        .ifPresent(
            tbc -> {
              if (tbc.isActive()) return;
              PositionComponent pc = beam.fetch(PositionComponent.class).get();
              Point to = calculateEndPoint(pc.position(), pc.viewDirection());
              int totalPoints = calculateNumberOfPoints(pc.position(), to);
              for (int i = 0; i < totalPoints; i++) {
                Entity beamElement =
                    TractorBeamFactory.createNextEntity(
                        pc.position().translate(pc.viewDirection().scale(i)),
                        pc.viewDirection(),
                        tbc);
                Game.add(beamElement);
              }
              configureCollider(beam, pc.viewDirection(), pc.position(), to);
              tbc.setActive(true);
            });
  }

  /** Deactivates the TractorBeam if not already deactivated. */
  public static void deactivate(Entity beam) {
    beam.fetch(TractorBeamComponent.class)
        .ifPresent(
            tbc -> {
              if (!tbc.isActive()) return;
              for (Entity e : getRelevantEntities(tbc)) {
                boolean isEmitter = "beamEmitter".equals(e.name());
                if (!isEmitter) {
                  Game.remove(e);
                } else {
                  e.fetch(CollideComponent.class)
                      .ifPresent(
                          collideComponent -> {
                            collideComponent.collider(new Hitbox(0, 0));
                            PositionSync.syncPosition(e);
                          });
                }
              }

              tbc.setActive(false);
            });
  }

  /**
   * Removes all entities from the given list that come after the first `beamEmitter` entity.
   *
   * <p>The first `beamEmitter` and all entities before it remain in the list.
   */
  public static void trimAfterFirstBeamEmitter(TractorBeamComponent tbc) {
    List<Entity> entities = getRelevantEntities(tbc);
    int firstEmitterIndex = -1;

    for (int i = 0; i < entities.size(); i++) {
      if ("beamEmitter".equals(entities.get(i).name())) {
        firstEmitterIndex = i;
        break;
      }
    }

    // remove all entities after first emitter
    if (firstEmitterIndex != -1 && firstEmitterIndex + 1 < entities.size()) {
      List<Entity> toRemove =
          new ArrayList<>(entities.subList(firstEmitterIndex + 1, entities.size()));
      for (Entity entity : toRemove) {
        Game.remove(entity);
      }
      entities.subList(firstEmitterIndex + 1, entities.size()).clear();
    }
  }

  /**
   * Calculates the number of points (entities) between {@code from} and {@code to}.
   *
   * @param from the starting point
   * @param to the end point
   * @return the number of interpolated points, including both start and end
   */
  public static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /**
   * Determines the last available point for a tractor beam. The beam is stopped by a wallTile, so
   * the EndPoint has to be last point in front of a wall tile.
   *
   * <p>Allow PortalTile on the first and second iteration (start or immediate portal exit).
   *
   * @param from the starting point
   * @param beamDirection the emitted direction of the tractor beam
   * @return the last available point
   */
  public static Point calculateEndPoint(Point from, Direction beamDirection) {
    Point lastPoint = from;
    Tile lastTile = Game.tileAt(lastPoint).orElse(null);
    Point currentPoint = from;
    Tile currentTile = Game.tileAt(from).orElse(null);
    boolean firstStep = true;
    boolean secondStep = false;

    while (currentTile != null
        && !currentTile.getClass().equals(WallTile.class)
        && !currentTile.getClass().equals(GlasswandTile.class)
        && (firstStep || secondStep || !lastTile.getClass().equals(PortalTile.class))) {
      secondStep = firstStep;
      firstStep = false;
      lastPoint = currentPoint;
      lastTile = Game.tileAt(lastPoint).orElse(null);
      currentPoint = currentPoint.translate(beamDirection);
      currentTile = Game.tileAt(currentPoint).orElse(null);
    }
    return lastPoint;
  }

  /**
   * Creates a Collider for the Portals to collide with, with the given size position and direction.
   *
   * @param emitter The emitter that gets the new Collider.
   * @param direction Direction of the Collider.
   * @param start Start Point.
   * @param end End Point.
   */
  private static void configureCollider(
      Entity emitter, Direction direction, Point start, Point end) {
    float width = 1f, height = 1f, offsetX = 0f, offsetY = 0f;
    if (direction == Direction.LEFT || direction == Direction.RIGHT) {
      float len = Math.abs(end.x() - start.x()) + 1f;
      width = Math.max(1f, len);
      offsetX = (direction == Direction.LEFT) ? -(width - 1f) : 0f;
    } else if (direction == Direction.UP || direction == Direction.DOWN) {
      float len = Math.abs(end.y() - start.y()) + 1f;
      height = Math.max(1f, len);
      offsetY = (direction == Direction.DOWN) ? -(height - 1f) : 0f;
    }

    TriConsumer<Entity, Entity, Direction> onCollisionEnter =
        (you, other, direction1) -> {
          PositionComponent pc = you.fetch(PositionComponent.class).get();
          you.fetch(TractorBeamComponent.class).get().forceToApply(beamForce(pc.viewDirection()));
          you.fetch(TractorBeamComponent.class)
              .get()
              .reversedForceToApply(reversedBeamForce(pc.viewDirection()));
        };

    TriConsumer<Entity, Entity, Direction> onCollisionLeave =
        (you, other, direction1) -> {
          if (other.fetch(ProjectileComponent.class).isEmpty()) {
            other.remove(FlyComponent.class);
          }
          if (other.isPresent(VelocityComponent.class)) {
            other.fetch(VelocityComponent.class).get().currentVelocity(Vector2.ZERO);
            other.fetch(VelocityComponent.class).get().clearForces();
          }
        };

    emitter.add(
        new CollideComponent(
            Vector2.of(offsetX, offsetY),
            Vector2.of(width, height),
            onCollisionEnter,
            onCollisionLeave));
    emitter
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.onHold(createActionConsumer());
              cc.isSolid(false);
            });
  }

  private static TriConsumer<Entity, Entity, Direction> createActionConsumer() {
    return (you, other, collisionDir) ->
        other
            .fetch(VelocityComponent.class)
            .ifPresent(
                vc -> {
                  if (!you.fetch(TractorBeamComponent.class).get().isActive()) {
                    return;
                  }
                  if (you.fetch(TractorBeamComponent.class)
                      .get()
                      .forceToApply()
                      .equals(Vector2.ZERO)) {
                    return;
                  }
                  if (other.isPresent(PortalComponent.class)) {
                    return;
                  }
                  if (!other.isPresent(FlyComponent.class)) {
                    other.add(new FlyComponent());
                  }
                  Vector2 forceVector;
                  if (you.fetch(TractorBeamComponent.class).get().isReversed())
                    forceVector =
                        you.fetch(TractorBeamComponent.class).get().reversedForceToApply();
                  else forceVector = you.fetch(TractorBeamComponent.class).get().forceToApply();
                  if (you.fetch(TractorBeamComponent.class).get().oldForces.containsKey(other)) {
                    Vector2 oldForce =
                        you.fetch(TractorBeamComponent.class).get().oldForces.get(other);
                    if (oldForce.x() == 0 && forceVector.x() == 0) {
                      vc.applyForce(beamEmitterName, forceVector);
                    } else if (oldForce.y() == 0 && forceVector.y() == 0) {
                      vc.applyForce(beamEmitterName, forceVector);
                    }
                  } else {
                    vc.applyForce(beamEmitterName, forceVector);
                    you.fetch(TractorBeamComponent.class).get().oldForces.put(other, forceVector);
                  }
                });
  }

  private static Vector2 reversedBeamForce(Direction dir) {
    Object o;
    try {
      return PortalRegistry.getCalculations().reversedBeamForce(dir);
    } catch (Exception e) {
      if (PortalRegistry.isDebugMode()) e.printStackTrace();
      DialogUtils.showTextPopup(BEAMFORCE, "Code Error");
    }
    return Vector2.ZERO;
  }

  private static Vector2 beamForce(Direction dir) {
    Object o;
    try {
      return PortalRegistry.getCalculations().beamForce(dir);
    } catch (Exception e) {
      if (PortalRegistry.isDebugMode()) e.printStackTrace();
      DialogUtils.showTextPopup(BEAMFORCE, "Code Error");
    }
    return Vector2.ZERO;
  }

  /**
   * Returns the list of all the entities that form the tractor beam.
   *
   * @return the entire list of the entities.
   */
  public static List<Entity> getRelevantEntities(TractorBeamComponent tbc) {
    return Game.levelEntities(Set.of(TractorBeamComponent.class))
        .filter(entity -> entity.fetch(TractorBeamComponent.class).get().equals(tbc))
        .toList();
  }
}
