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
 * <p>CollisionSystem is a system which checks on execute whether the hitboxes of two entities are
 * overlapping/colliding. In which case the corresponding Methods are called on both entities.
 *
 * <p>The system does imply the hitboxes are axis aligned.
 *
 * <p>Each CollideComponent should only be informed when a collision begins or ends. For this a map
 * with all currently active collisions is stored and allows informing the entities when a collision
 * ended.
 */
public final class CollisionSystem extends System {

    private final Map<CollisionKey, CollisionData> collisions = new HashMap<>();

    public CollisionSystem() {
        super(CollideComponent.class);
    }

    /**
     * Test every CollideEntity with every other CollideEntity for collision.
     *
     * <p>The collision check will be performed only once for a given tuple of entities, i.e. when
     * entity A does collide with entity B it also means B collides with A.
     */
    @Override
    public void execute() {
        entityStream().flatMap(this::createDataPairs).forEach(this::onEnterLeaveCheck);
    }

    /**
     * Create a stream of pairs of entities.
     *
     * <p>Pair a given entity with every other entity with a higher id.
     *
     * @param a Entity which is the lower id partner
     * @return the stream which contains every valid pair of Entities
     */
    private Stream<CollisionData> createDataPairs(Entity a) {
        return entityStream().filter(b -> isSmallerThen(a, b)).map(b -> newDataPair(a, b));
    }

    /**
     * Compare the entities.
     *
     * <p>This comparison is applied in the {@link #createDataPairs(Entity a) createDataPairs}
     * method to create only tuples with entities with higher ID. This avoids performing a collision
     * check twice for a pair of entities, first for (a,b) and second for (b,a).
     *
     * @param a first Entity
     * @param b second Entity
     * @return true when the comparison between a and b is less than zero, otherwise false
     */
    private boolean isSmallerThen(Entity a, Entity b) {
        return a.compareTo(b) < 0;
    }

    /**
     * Create a pair of CollideComponents which is the used to check whether a collision is
     * happening and to store in the internal map. Which allows informing the CollideComponents
     * about an ended Collision
     *
     * @param a The first Entity
     * @param b the second Entity
     * @return the pair of CollideComponents
     */
    private CollisionData newDataPair(Entity a, Entity b) {
        CollideComponent cca =
                a.fetch(CollideComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(a, CollideComponent.class));
        CollideComponent ccb =
                b.fetch(CollideComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(b, CollideComponent.class));

        return new CollisionData(a, cca, b, ccb);
    }

    /**
     * Check whether a new collision is happening or whether a collision has ended.
     *
     * <p>Only allows a new collision to call the onEnter of the hitboxes. An ongoing collision is
     * not calling the onEnter of the hitboxes. When a previous collision existed and no longer is
     * an active collision the onLeave is called. The onLeave is only called once.
     *
     * @param cdata the CollisionData where a collision change may happen
     */
    private void onEnterLeaveCheck(CollisionData cdata) {
        CollisionKey key = new CollisionKey(cdata.ea.id(), cdata.eb.id());

        if (checkForCollision(cdata.ea, cdata.a, cdata.eb, cdata.b)) {
            // a collision is currently happening
            if (!collisions.containsKey(key)) {
                // a new collision should call the onEnter on both entities
                collisions.put(key, cdata);
                Tile.Direction d = checkDirectionOfCollision(cdata.ea, cdata.a, cdata.eb, cdata.b);
                cdata.a.onEnter(cdata.ea, cdata.eb, d);
                cdata.b.onEnter(cdata.eb, cdata.ea, inverse(d));
            }
        } else if (collisions.remove(key) != null) {
            // a collision was happening and the two entities are no longer colliding on Leave
            // called once
            Tile.Direction d = checkDirectionOfCollision(cdata.ea, cdata.a, cdata.eb, cdata.b);
            cdata.a.onLeave(cdata.ea, cdata.eb, d);
            cdata.b.onLeave(cdata.eb, cdata.ea, inverse(d));
        }
    }

    /**
     * Simple Direction inversion
     *
     * @param d to inverse
     * @return the opposite direction
     */
    protected Tile.Direction inverse(Tile.Direction d) {
        return switch (d) {
            case N -> Tile.Direction.S;
            case E -> Tile.Direction.W;
            case S -> Tile.Direction.N;
            case W -> Tile.Direction.E;
        };
    }

    /**
     * Check if two hitboxes intersect
     *
     * @param hitbox1
     * @param hitbox2
     * @return true if intersection exists otherwise false
     */
    protected boolean checkForCollision(
            Entity h1, CollideComponent hitbox1, Entity h2, CollideComponent hitbox2) {
        return hitbox1.bottomLeft(h1).x < hitbox2.topRight(h2).x
                && hitbox1.topRight(h1).x > hitbox2.bottomLeft(h2).x
                && hitbox1.bottomLeft(h1).y < hitbox2.topRight(h2).y
                && hitbox1.topRight(h1).y > hitbox2.bottomLeft(h2).y;
    }

    /**
     * Calculates the direction based on a square, can be broken once the hitboxes are rectangular.
     *
     * @param hitbox1
     * @param hitbox2
     * @return Tile direction for where hitbox 2 is compared to hitbox 1
     */
    protected Tile.Direction checkDirectionOfCollision(
            Entity h1, CollideComponent hitbox1, Entity h2, CollideComponent hitbox2) {
        float y = hitbox2.center(h2).y - hitbox1.center(h1).y;
        float x = hitbox2.center(h2).x - hitbox1.center(h1).x;
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
