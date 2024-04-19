package contrib.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.System;
import core.level.Tile;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Map;
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
    entityStream().flatMap(this::createDataPairs).forEach(this::onEnterLeaveCheck);
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
    return entityStream().filter(b -> isSmallerThen(a, b)).map(b -> newDataPair(a, b));
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
      // a new collision should call the onEnter on both entities
      collisions.put(key, cdata);
      Tile.Direction d = checkDirectionOfCollision(cdata.ea, cdata.a, cdata.eb, cdata.b);
      cdata.a.onEnter(cdata.ea, cdata.eb, d);
      cdata.b.onEnter(cdata.eb, cdata.ea, inverse(d));

    } else if (collisions.remove(key) != null) {
      // a collision was happening and the two entities are no longer colliding, on Leave
      // called once
      Tile.Direction d = checkDirectionOfCollision(cdata.ea, cdata.a, cdata.eb, cdata.b);
      cdata.a.onLeave(cdata.ea, cdata.eb, d);
      cdata.b.onLeave(cdata.eb, cdata.ea, inverse(d));
    }
  }

  /**
   * Simple Direction inversion.
   *
   * @param d Direction to inverse.
   * @return The opposite direction.
   */
  Tile.Direction inverse(final Tile.Direction d) {
    return switch (d) {
      case N -> Tile.Direction.S;
      case E -> Tile.Direction.W;
      case S -> Tile.Direction.N;
      case W -> Tile.Direction.E;
    };
  }

  /**
   * Check if two hitBoxes intersect.
   *
   * @param h1 WTF? .
   * @param hitBox1 First hitBox.
   * @param h2 WTF? .
   * @param hitBox2 Second hitBox.
   * @return true if intersection exists, otherwise false.
   */
  boolean checkForCollision(
      final Entity h1,
      final CollideComponent hitBox1,
      final Entity h2,
      final CollideComponent hitBox2) {
    return hitBox1.bottomLeft(h1).x < hitBox2.topRight(h2).x
        && hitBox1.topRight(h1).x > hitBox2.bottomLeft(h2).x
        && hitBox1.bottomLeft(h1).y < hitBox2.topRight(h2).y
        && hitBox1.topRight(h1).y > hitBox2.bottomLeft(h2).y;
  }

  /**
   * Calculates the direction based on a square, can be broken once the hitBoxes are rectangular.
   *
   * @param h1 WTF? .
   * @param hitBox1 The first hitBox.
   * @param h2 WTF? .
   * @param hitBox2 The second hitBox.
   * @return Tile direction for where hitBox2 is compared to hitBox1.
   */
  Tile.Direction checkDirectionOfCollision(
      final Entity h1,
      final CollideComponent hitBox1,
      final Entity h2,
      final CollideComponent hitBox2) {
    float y = hitBox2.center(h2).y - hitBox1.center(h1).y;
    float x = hitBox2.center(h2).x - hitBox1.center(h1).x;
    float rads = (float) Math.atan2(y, x);
    double piQuarter = Math.PI / 4;
    if (rads < 3 * -piQuarter) {
      return Tile.Direction.W;
    } else if (rads < -piQuarter) {
      return Tile.Direction.N;
    } else if (rads < piQuarter) {
      return Tile.Direction.E;
    } else if (rads < 3 * piQuarter) {
      return Tile.Direction.S;
    } else {
      return Tile.Direction.W;
    }
  }

  private record CollisionKey(int a, int b) {}

  protected record CollisionData(Entity ea, CollideComponent a, Entity eb, CollideComponent b) {}
}
