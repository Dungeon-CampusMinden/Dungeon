package ecs.systems;

import ecs.components.HitboxComponent;
import ecs.entities.Entity;
import java.lang.reflect.InvocationTargetException;
import level.elements.tile.Tile;
import starter.Game;

/** System to check for collisions between two entities */
public class CollisionSystem extends ECS_System {

    /** checks if there is a collision between two entities based on their hitbox */
    @Override
    public void update() {
        for (Entity entity : Game.entities) {

            entity.getComponent(HitboxComponent.class)
                    .ifPresent(
                            hitbox1 -> {
                                for (Entity entity2 : Game.entities) {
                                    if (entity != entity2) {

                                        entity2.getComponent(HitboxComponent.class)
                                                .ifPresent(
                                                        hitbox2 -> {
                                                            if (checkForCollision(
                                                                    (HitboxComponent) hitbox1,
                                                                    (HitboxComponent) hitbox2)) {
                                                                Tile.Direction d =
                                                                        checkDirectionOfCollision(
                                                                                (HitboxComponent)
                                                                                        hitbox1,
                                                                                (HitboxComponent)
                                                                                        hitbox2);
                                                                try {
                                                                    ((HitboxComponent) hitbox1)
                                                                            .collide(
                                                                                    (HitboxComponent)
                                                                                            hitbox2,
                                                                                    d);
                                                                } catch (
                                                                        InvocationTargetException
                                                                                e) {
                                                                    throw new RuntimeException(e);
                                                                } catch (IllegalAccessException e) {
                                                                    throw new RuntimeException(e);
                                                                }
                                                                try {
                                                                    ((HitboxComponent) hitbox2)
                                                                            .collide(
                                                                                    (HitboxComponent)
                                                                                            hitbox1,
                                                                                    inverse(d));
                                                                } catch (
                                                                        InvocationTargetException
                                                                                e) {
                                                                    throw new RuntimeException(e);
                                                                } catch (IllegalAccessException e) {
                                                                    throw new RuntimeException(e);
                                                                }
                                                            }
                                                        });
                                    }
                                }
                            });
        }
    }

    /**
     * Simple Direction inversion
     *
     * @param d to inverse
     * @return the oposite direction
     */
    private Tile.Direction inverse(Tile.Direction d) {
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
    private boolean checkForCollision(HitboxComponent hitbox1, HitboxComponent hitbox2) {
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
    private Tile.Direction checkDirectionOfCollision(
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
        } else {
            return Tile.Direction.S;
        }
    }
}
