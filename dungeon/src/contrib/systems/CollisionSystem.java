package contrib.systems;

import contrib.components.CollideComponent;
import contrib.utils.components.collide.Collider;
import contrib.utils.components.collide.CollisionUtils;
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
  public static final float COLLIDE_SET_DISTANCE = 0.01f;

  private final Map<CollisionKey, CollisionData> collisions = new HashMap<>();

  /** Create a new CollisionSystem. */
  public CollisionSystem() {
    super(CollideComponent.class);
    onEntityAdd = this::onAddEntity;
  }

  private void onAddEntity(Entity e) {
    PositionSync.syncPosition(e);
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

    if (checkForCollision(cdata.a, cdata.b)) {
      Direction d = checkDirectionOfCollision(cdata.a.collider(), cdata.b.collider());
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
      Direction d = checkDirectionOfCollision(cdata.a.collider(), cdata.b.collider());
      // a collision was happening and the two entities are no longer colliding, on Leave
      // called once
      cdata.a.onLeave(cdata.ea, cdata.eb, d);
      cdata.b.onLeave(cdata.eb, cdata.ea, d.opposite());
    }
  }

  private void checkSolidCollision(CollisionData cdata, Direction d) {
    VelocityComponent vca = cdata.ea.fetch(VelocityComponent.class).orElse(null);
    boolean aStationary = vca == null || vca.maxSpeed() == 0f;
    VelocityComponent vcb = cdata.eb.fetch(VelocityComponent.class).orElse(null);
    boolean bStationary = vcb == null || vcb.maxSpeed() == 0f;

    if (aStationary && bStationary) {
      LOGGER.warning(
          "Two stationary solid entities are colliding: " + cdata.ea + " and " + cdata.eb);
    } else if (aStationary) {
      solidCollide(cdata.ea, cdata.a.collider(), cdata.eb, cdata.b.collider(), d);
    } else if (bStationary) {
      solidCollide(cdata.eb, cdata.b.collider(), cdata.ea, cdata.a.collider(), d.opposite());
    } else {
      // Determine which entity moves based on their weight. The heavier entity
      // moves the lighter one.
      if (vca.mass() > vcb.mass()) {
        solidCollide(cdata.ea, cdata.a.collider(), cdata.eb, cdata.b.collider(), d);
      } else {
        solidCollide(cdata.eb, cdata.b.collider(), cdata.ea, cdata.a.collider(), d.opposite());
      }
    }
  }

  /**
   * Check if two hitBoxes intersect.
   *
   * @param cc1 First entity's CollideComponent.
   * @param cc2 Second entity's CollideComponent.
   * @return true if intersection exists, otherwise false.
   */
  boolean checkForCollision(final CollideComponent cc1, final CollideComponent cc2) {
    return cc1.collider().collide(cc2.collider());
  }

  /**
   * Calculates the direction of a collision.
   *
   * @param a First entity's CollideComponent.
   * @param b Second entity's CollideComponent.
   * @return Direction of the collision between the entities
   */
  Direction checkDirectionOfCollision(Collider a, Collider b) {
    Vector2 c1HalfSize = a.halfSize();
    Vector2 c2HalfSize = b.halfSize();

    Point c1Center = a.absoluteCenter();
    Point c2Center = b.absoluteCenter();

    // Take the distance between the center of both hitboxes in both X and Y direction
    float dx = c1Center.x() - c2Center.x();
    float dy = c1Center.y() - c2Center.y();

    // Sum of the half widths and half heights = distance between the entities' centers if they are
    // flush with each other
    float halfXWidths = c1HalfSize.x() + c2HalfSize.x();
    float halfYHeights = c1HalfSize.y() + c2HalfSize.y();

    // To get the overlap, we subtract the actual distance between the centers. If the entities are
    // overlapping, this will be a positive number
    float overlapX = halfXWidths - Math.abs(dx);
    float overlapY = halfYHeights - Math.abs(dy);

    // Check which overlap is bigger to determine the axis of collision, then determine the
    // direction by checking which side the first hitbox is on
    if (overlapX < overlapY) {
      return dx > 0 ? Direction.LEFT : Direction.RIGHT;
    } else {
      return dy > 0 ? Direction.DOWN : Direction.UP;
    }
  }

  private void solidCollide(Entity ea, Collider a, Entity eb, Collider b, Direction direction) {
    solidCollide(ea, a, eb, b, direction, true);
  }

  private void solidCollide(
      Entity ea, Collider a, Entity eb, Collider b, Direction direction, boolean firstCollision) {
    Point c1Pos = a.absolutePosition();
    Vector2 c1Size = a.size();
    Point c2Pos = b.absolutePosition();
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
      LOGGER.severe("Direction was NONE in solid collision, this should never happen!");
      return;
    }

    Point newPos = newColliderPos.translate(b.offset().inverse());

    boolean bCanEnterOpenPits =
        eb.fetch(VelocityComponent.class).map(VelocityComponent::canEnterOpenPits).orElse(false);
    boolean bCanEnterWalls =
        eb.fetch(VelocityComponent.class).map(VelocityComponent::canEnterWalls).orElse(false);
    boolean bCanEnterGitter =
        eb.fetch(VelocityComponent.class).map(VelocityComponent::canEnterGitter).orElse(false);
    boolean bCanEnterGlasswalls =
        eb.fetch(VelocityComponent.class).map(VelocityComponent::canEnterGlasswalls).orElse(false);
    if (CollisionUtils.isCollidingWithLevel(
        b, newPos, bCanEnterOpenPits, bCanEnterWalls, bCanEnterGitter, bCanEnterGlasswalls)) {
      if (firstCollision) {
        // If the new position collides with the level, block the other entity instead.
        solidCollide(eb, b, ea, a, direction.opposite(), false);
      }
      // If we aren't in the first iteration, the other entity is also blocked, so just don't do
      // anything
      return;
    }

    eb.fetch(PositionComponent.class).orElseThrow().position(newPos);
    PositionSync.syncPosition(eb);
  }

  private record CollisionKey(int a, int b) {}

  protected record CollisionData(Entity ea, CollideComponent a, Entity eb, CollideComponent b) {}
}
