package contrib.systems;

import contrib.components.CollideComponent;

import core.Entity;
import core.System;
import core.level.Tile;
import core.utils.components.MissingComponentException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/** System to check for collisions between two entities */
public final class CollisionSystem extends System {

    private final Map<CollisionKey, CollisionData> collisions = new HashMap<>();

    public CollisionSystem() {
        super(CollideComponent.class);
    }

    /**
     * Test every CollideEntity with each other CollideEntity for collision.
     *
     * <p>The collision check will be performed only once for a given tuple of entities. When entity
     * A does collide with entity B it also means B collides with A.
     */
    @Override
    public void execute() {
        getEntityStream().flatMap(this::createDataPairs).forEach(this::onEnterLeaveCheck);
    }

    /**
     * Create a stream of pairs of entities
     *
     * <p>Pair a given entity with every other entity with a higher id.
     *
     * @param a Entity which is the lower id partner
     * @return the stream which contains every valid pair of Entities
     */
    private Stream<CollisionData> createDataPairs(Entity a) {
        return getEntityStream().filter(b -> isIDSmallerThen(a, b)).map(b -> newDataPair(a, b));
    }

    /**
     * Compares the id of the given Entities.
     *
     * <p>Makes sure only Entities with a higher id are paired up. This Prevents a pair of Entities
     * to be checked double or with itself.
     *
     * @param a first Entity
     * @param b second Entity
     * @return true when the id of a is smaller than the one from b, otherwise false
     */
    private boolean isIDSmallerThen(Entity a, Entity b) {
        return a.id() < b.id();
    }

    /**
     * Create a pair of CollideComponents
     *
     * @param a
     * @param b
     * @return
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

        return new CollisionData(cca, ccb);
    }

    /**
     * Checks whether a new collision is happening or whether a collision has ended.
     *
     * <p>Only allows a new collision to call the onEnter of the hitboxes. An ongoing collision is
     * not calling the onEnter of the hitboxes. When a previous collision existed and no longer is
     * an active collision the onLeave is called. The onLeave is only called once.
     *
     * @param cdata the CollisionData where a collision change may happen
     */
    private void onEnterLeaveCheck(CollisionData cdata) {
        CollisionKey key = new CollisionKey(cdata.a.entity().id(), cdata.b.entity().id());

        if (checkForCollision(cdata.a, cdata.b)) {
            // a collision is currently happening
            if (!collisions.containsKey(key)) {
                // a new collision should call the onEnter on both entities
                collisions.put(key, cdata);
                Tile.Direction d = checkDirectionOfCollision(cdata.a, cdata.b);
                cdata.a.onEnter(cdata.b, d);
                cdata.b.onEnter(cdata.a, inverse(d));
            }
        } else if (collisions.remove(key) != null) {
            // a collision was happening and the two entities are no longer colliding on Leave
            // called once
            Tile.Direction d = checkDirectionOfCollision(cdata.a, cdata.b);
            cdata.a.onLeave(cdata.b, d);
            cdata.b.onLeave(cdata.b, inverse(d));
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
    protected boolean checkForCollision(CollideComponent hitbox1, CollideComponent hitbox2) {
        return hitbox1.bottomLeft().x < hitbox2.topRight().x
                && hitbox1.topRight().x > hitbox2.bottomLeft().x
                && hitbox1.bottomLeft().y < hitbox2.topRight().y
                && hitbox1.topRight().y > hitbox2.bottomLeft().y;
    }

    /**
     * Calculates the direction based on a square, can be broken once the hitboxes are rectangular.
     *
     * @param hitbox1
     * @param hitbox2
     * @return Tile direction for where hitbox 2 is compared to hitbox 1
     */
    protected Tile.Direction checkDirectionOfCollision(
            CollideComponent hitbox1, CollideComponent hitbox2) {
        float y = hitbox2.center().y - hitbox1.center().y;
        float x = hitbox2.center().x - hitbox1.center().x;
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

    protected record CollisionData(CollideComponent a, CollideComponent b) {}
}
