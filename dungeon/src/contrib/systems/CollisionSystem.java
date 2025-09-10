package contrib.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.*;
import java.util.stream.Stream;

/**
 * System to check for collisions between two entities.
 *
 * <p>CollisionSystem is a system which checks on execute whether the hit boxes of two entities are
 * overlapping/colliding. In which case the corresponding Methods are called on both entities.
 *
 * <p>The system does imply the hit boxes are axis aligned.
 *
 * <p>Each CollideComponent should only be informed when a collision begins or ends. For this, a map
 * with all currently active collisions is stored and allows informing the entities when a collision
 * ended.
 *
 * <p>Entities with the {@link CollideComponent} will be processed by this system.
 */
public final class CollisionSystem extends System {

  /** Solid entities will be kept at this distance after colliding. */
  private static final float COLLIDE_SET_DISTANCE = 0.01f;

  private final Map<CollisionKey, CollisionData> collisions = new HashMap<>();

  /** Create a new CollisionSystem. */
  public CollisionSystem() {
    super(CollideComponent.class);
  }

  /**
   * Test every CollideEntity with every other CollideEntity for collision.
   *
   * <p>The collision check will be performed only once for a given tuple of entities, i.e. when
   * entity A does collide with entity B, it also means B collides with A.
   */
  @Override
  public void execute() {
    filteredEntityStream(CollideComponent.class)
        .flatMap(this::createDataPairs)
        .forEach(this::onEnterLeaveCheck);
  }

  /**
   * Create a stream of pairs of entities.
   *
   * <p>Pair a given entity with every other entity with a higher ID.
   *
   * @param a Entity which is the lower ID partner.
   * @return The stream which contains every valid pair of Entities.
   */
  private Stream<CollisionData> createDataPairs(final Entity a) {
    return filteredEntityStream().filter(b -> isSmallerThen(a, b)).map(b -> newDataPair(a, b));
  }

  /**
   * Compare the entities.
   *
   * <p>This comparison is applied in the {@link #createDataPairs(Entity a) createDataPairs} method
   * to create only tuples with entities with higher ID. This avoids performing a collision check
   * twice for a pair of entities, first for (a,b) and second for (b,a).
   *
   * @param a First Entity.
   * @param b Second Entity
   * @return true when the comparison between a and b is less than zero, otherwise false.
   */
  private boolean isSmallerThen(final Entity a, final Entity b) {
    return a.compareTo(b) < 0;
  }

  /**
   * Create a pair of CollideComponents which is then used to check whether a collision is happening
   * and to store in the internal map. Which allows informing the CollideComponents about an ended
   * Collision.
   *
   * @param a The first Entity.
   * @param b the second Entity.
   * @return The pair of CollideComponents.
   */
  private CollisionData newDataPair(final Entity a, final Entity b) {
    CollideComponent cca =
        a.fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(a, CollideComponent.class));
    CollideComponent ccb =
        b.fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(b, CollideComponent.class));

    return new CollisionData(a, cca, b, ccb);
  }

  /**
   * Check whether a new collision is happening or whether a collision has ended.
   *
   * <p>Only allows a new collision to call the onEnter of the hitBoxes. An ongoing collision is not
   * calling the onEnter of the hitBoxes. When a previous collision existed and no longer is an
   * active collision, onLeave is called. onLeave is only called once.
   *
   * @param cdata The CollisionData where a collision change may happen.
   */
  private void onEnterLeaveCheck(final CollisionData cdata) {
    CollisionKey key = new CollisionKey(cdata.ea.id(), cdata.eb.id());

    if (checkForCollision(cdata.ea, cdata.a, cdata.eb, cdata.b)) {
      Direction d = checkDirectionOfCollision(cdata.ea, cdata.a, cdata.eb, cdata.b);
      // a collision is currently happening
      if (!collisions.containsKey(key)) {
        // a new collision should call the onEnter on both entities
        collisions.put(key, cdata);
        cdata.a.onEnter(cdata.ea, cdata.eb, d);
        cdata.b.onEnter(cdata.eb, cdata.ea, d.opposite());
      }
      // collision is ongoing
      cdata.a.onHold(cdata.ea, cdata.eb, d);
      cdata.b.onHold(cdata.eb, cdata.ea, d.opposite());

      // Check if both entities are solids, and if so, separate them
      if (cdata.a.isSolid() && cdata.b.isSolid()) {
        checkSolidCollision(cdata, d);
      }

    } else if (collisions.remove(key) != null) {
      Direction d = checkDirectionOfCollision(cdata.ea, cdata.a, cdata.eb, cdata.b);
      // a collision was happening and the two entities are no longer colliding, on Leave
      // called once
      cdata.a.onLeave(cdata.ea, cdata.eb, d);
      cdata.b.onLeave(cdata.eb, cdata.ea, d.opposite());
    }
  }

  private void checkSolidCollision(CollisionData cdata, Direction d) {
    if (cdata.a.isStationary() && cdata.b.isStationary()) {
      LOGGER.warning(
          "Two stationary solid entities are colliding: " + cdata.ea + " and " + cdata.eb);
    } else if (cdata.a.isStationary()) {
      solidCollide(cdata.ea, cdata.a, cdata.eb, cdata.b, d);
    } else if (cdata.b.isStationary()) {
      solidCollide(cdata.eb, cdata.b, cdata.ea, cdata.a, d.opposite());
    } else {
      // Two non-stationary solids collide.
      cdata
          .ea
          .fetch(VelocityComponent.class)
          .ifPresent(
              v1 -> {
                cdata
                    .eb
                    .fetch(VelocityComponent.class)
                    .ifPresent(
                        v2 -> {
                          // Determine which entity moves based on their weight. The heavier entity
                          // moves the lighter one.
                          if (v1.mass() >= v2.mass()) {
                            solidCollide(cdata.ea, cdata.a, cdata.eb, cdata.b, d);
                          } else {
                            solidCollide(cdata.eb, cdata.b, cdata.ea, cdata.a, d.opposite());
                          }
                        });
              });
    }
  }

  /**
   * Check if two hitBoxes intersect.
   *
   * @param h1 First entity.
   * @param hitBox1 First hitBox.
   * @param h2 Second entity.
   * @param hitBox2 Second hitBox.
   * @return true if intersection exists, otherwise false.
   */
  boolean checkForCollision(
      final Entity h1,
      final CollideComponent hitBox1,
      final Entity h2,
      final CollideComponent hitBox2) {
    return hitBox1.bottomLeft(h1).x() < hitBox2.topRight(h2).x()
        && hitBox1.topRight(h1).x() > hitBox2.bottomLeft(h2).x()
        && hitBox1.bottomLeft(h1).y() < hitBox2.topRight(h2).y()
        && hitBox1.topRight(h1).y() > hitBox2.bottomLeft(h2).y();
  }

  /**
   * Calculates the direction of a collision.
   *
   * @param ea First entity.
   * @param a First entity's hitBox.
   * @param eb Second entity.
   * @param b Second entity's hitBox.
   * @return Direction of the collision between the entities
   */
  Direction checkDirectionOfCollision(
      Entity ea, CollideComponent a, Entity eb, CollideComponent b) {
    Point c1Pos = a.bottomLeft(ea);
    Vector2 c1Size = a.size();
    Point c2Pos = b.bottomLeft(eb);
    Vector2 c2Size = b.size();

    float dx = (c1Pos.x() + c1Size.x() / 2f) - (c2Pos.x() + c2Size.x() / 2f);
    float dy = (c1Pos.y() + c1Size.y() / 2f) - (c2Pos.y() + c2Size.y() / 2f);

    float overlapX = (c1Size.x() / 2f + c2Size.x() / 2f) - Math.abs(dx);
    float overlapY = (c1Size.y() / 2f + c2Size.y() / 2f) - Math.abs(dy);

    if (overlapX < overlapY) {
      return dx > 0 ? Direction.LEFT : Direction.RIGHT;
    } else {
      return dy > 0 ? Direction.DOWN : Direction.UP;
    }
  }

  private void solidCollide(
      Entity ea, CollideComponent a, Entity eb, CollideComponent b, Direction direction) {
    Point c1Pos = a.bottomLeft(ea);
    Vector2 c1Size = a.size();
    Point c2Pos = b.bottomLeft(eb);
    Vector2 c2Size = b.size();

    Point newColliderPos =
        switch (direction) {
          case DOWN -> new Point(c2Pos.x(), c1Pos.y() - c2Size.y() - COLLIDE_SET_DISTANCE);
          case LEFT -> new Point(c1Pos.x() - c2Size.x() - COLLIDE_SET_DISTANCE, c2Pos.y());
          case UP -> new Point(c2Pos.x(), c1Pos.y() + c1Size.y() + COLLIDE_SET_DISTANCE);
          case RIGHT -> new Point(c1Pos.x() + c1Size.x() + COLLIDE_SET_DISTANCE, c2Pos.y());
          case NONE -> null;
        };

    if (newColliderPos == null) {
      LOGGER.warning("Direction was NONE in solid collision, this should never happen!");
      return;
    }

    Point newPos = newColliderPos.translate(b.offset().inverse());
    //    Point newPos = newColliderPos;

    eb.fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              pc.position(newPos);
            });
  }

  private record CollisionKey(int a, int b) {}

  protected record CollisionData(Entity ea, CollideComponent a, Entity eb, CollideComponent b) {}
}
