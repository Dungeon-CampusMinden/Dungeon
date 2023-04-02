package ecs.systems;

import ecs.components.HitboxComponent;
import java.util.HashMap;
import java.util.Map;
import level.elements.tile.Tile;
import starter.Game;

/** System to check for collisions between two entities */
public class CollisionSystem extends ECS_System {

    private record CollisionKey(int a, int b) {}

    protected record CollisionData(HitboxComponent a, HitboxComponent b) {}

    private Map<CollisionKey, CollisionData> collisions = new HashMap<>();

    /** checks if there is a collision between two entities based on their hitbox */
    @Override
    public void update() {
        Game.getEntities().stream()
                .flatMap(
                        a ->
                                a
                                        .getComponent(HitboxComponent.class)
                                        .map(HitboxComponent.class::cast)
                                        .stream())
                .flatMap(
                        a ->
                                Game.getEntities().stream()
                                        .filter(b -> a.getEntity().id < b.id)
                                        .flatMap(
                                                b ->
                                                        b
                                                                .getComponent(HitboxComponent.class)
                                                                .map(HitboxComponent.class::cast)
                                                                .stream())
                                        .map(b -> buildData(a, b)))
                .forEach(this::onEnterLeaveCheck);
    }

    private CollisionData buildData(HitboxComponent a, HitboxComponent b) {
        return new CollisionData(a, b);
    }

    private void onEnterLeaveCheck(CollisionData cdata) {
        CollisionKey key = new CollisionKey(cdata.a.getEntity().id, cdata.b.getEntity().id);

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
     * @return the oposite direction
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
     * The Check if hitbox intersect
     *
     * @param hitbox1
     * @param hitbox2
     * @return true if intersection exists otherwise false
     */
    protected boolean checkForCollision(HitboxComponent hitbox1, HitboxComponent hitbox2) {
        return hitbox1.getBottomLeft().x < hitbox2.getTopRight().x
                && hitbox1.getTopRight().x > hitbox2.getBottomLeft().x
                && hitbox1.getBottomLeft().y < hitbox2.getTopRight().y
                && hitbox1.getTopRight().y > hitbox2.getBottomLeft().y;
    }

    /**
     * Calculates the direction based on a square, can be broken once the hitboxes are rectangular.
     *
     * @param hitbox1
     * @param hitbox2
     * @return Tile direction for where hitbox 2 is compared to hitbox 1
     */
    protected Tile.Direction checkDirectionOfCollision(
            HitboxComponent hitbox1, HitboxComponent hitbox2) {
        float y = hitbox2.getCenter().y - hitbox1.getCenter().y;
        float x = hitbox2.getCenter().x - hitbox1.getCenter().x;
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
}
