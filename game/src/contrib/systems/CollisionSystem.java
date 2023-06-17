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
     * checks every CollideEntity with each other CollideEntity and checks them for a collision.
     *
     * <p>Some improvements are done so there is only one collision check between two Entities based
     * on their IDs. When Entity A does collide with Entity B it also means B collides with A.
     */
    @Override
    public void execute() {
        getEntityStream().flatMap(this::createDataPairs).forEach(this::onEnterLeaveCheck);
    }

    /**
     * Creates a Stream where every other Entity has a higher id.
     *
     * @param a Entity which is the lower id partner
     * @return the stream which contains every valid pair of Entities
     */
    private Stream<CollisionData> createDataPairs(Entity a) {
        return getEntityStream().filter(b -> isIDSmallerThen(a, b)).map(b -> newDataPair(a, b));
    }

    /**
     * Makes sure only Entities with a higher id are paired up.
     *
     * <p>This Prevents a pair of Entities to be checked double or with itself.
     *
     * @param a first Entity
     * @param b second Entity
     * @return true when the id of b is bigger then the one from a
     */
    private static boolean isIDSmallerThen(Entity a, Entity b) {
        return a.id() < b.id();
    }

    /**
     * Creates a basic Pair of CollideComponents to allow one Stream to compute every Collide check
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
     * Checks wheter a new Collision is happening or wheter a Collision ended
     *
     * @param cdata
     */
    private void onEnterLeaveCheck(CollisionData cdata) {
        CollisionKey key = new CollisionKey(cdata.a.entity().id(), cdata.b.entity().id());

        if (checkForCollision(cdata.a, cdata.b)) {
            if (!collisions.containsKey(key)) {
                collisions.put(key, cdata);
                Tile.Direction d = checkDirectionOfCollision(cdata.a, cdata.b);
                cdata.a.onEnter(cdata.b, d);
                cdata.b.onEnter(cdata.a, inverse(d));
            }
        } else if (collisions.remove(key) != null) {
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
     * The Check if hitboxes intersect
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
