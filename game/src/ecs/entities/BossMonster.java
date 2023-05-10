package ecs.entities;

import tools.Point;

public abstract class BossMonster extends Monster {
    /**
     * create a Monster of type BossMonster
     *
     * @param xSpeed speed in x-direction
     * @param ySpeed speed in y-direction
     * @param MAX_HEALTH max health
     * @param attackRange range in which to attack
     * @param pathToIdleLeft path to left-facing idle animation
     * @param pathToIdleRight path to right-facing idle animation
     * @param pathToRunLeft path to left-facing running animation
     * @param pathToRunRight path to right-facing running animation
     * @param position position at which to be created
     */
    public BossMonster (float xSpeed,
                        float ySpeed,
                        int MAX_HEALTH,
                        float attackRange,
                        String pathToIdleLeft,
                        String pathToIdleRight,
                        String pathToRunLeft,
                        String pathToRunRight,
                        Point position
                        ) {
        super(xSpeed, ySpeed, MAX_HEALTH, attackRange, pathToIdleLeft,
            pathToIdleRight, pathToRunLeft, pathToRunRight, position);
    }
}
