package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Boss;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Point;

import java.util.logging.Logger;

/**
 * The BossWalk is a class which implements the IIdleAI interface.
 * It is used to make the Boss walk around in the level.
 * It has a path, which is calculated by the AITools class.
 * It has a currentBreak, which is used to make the Boss wait for a certain amount of time.
 * It has a BREAK_TIME, which is the time the Boss waits.
 * It has an aggressive, which is used to check if the Boss is aggressive.
 * It has a range, which is the range the Boss can see the Hero.
 * It has an entity, which is the Boss.
 */

public class BossWalk implements IIdleAI {
    private GraphPath<Tile> path;
    private static final float MAX_RADIUS = 10f;
    private static final float MIN_RADIUS = 5f;
    private transient final Logger bossWalkLogger = Logger.getLogger(this.getClass().getName());

    /**
     * The Boss is in 2 of 99 times walking. The Boss is walking to a random Point (with a distance of 5 to 10 tiles).
     */

    public BossWalk(){
        bossWalkLogger.info("BossWalk created");
    }
    @Override
    public void idle(Entity entity) {
        bossWalkLogger.info("BossWalk idle");
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            // is not on path
            bossWalkLogger.info("BossWalk idle: path is null or path is finished or left");
            int random = (int) (Math.random() * 100);
            if (random > 97) {// 3% chance
                Point entityPoint = entityPosition(entity);
                Point newPosition = entityPoint;
                while (AITools.inRange(entityPoint, newPosition, MIN_RADIUS)) {
                    // must be at least 5 tiles distance
                    newPosition = AITools.getRandomAccessibleTileCoordinateInRange(entityPoint, MAX_RADIUS).toPoint();
                    // looks for a point within 10 tiles radius
                }
                path = AITools.calculatePath(entityPoint, newPosition);
            }
            return;
        }
        // is on path
        AITools.move(entity, path);
        bossWalkLogger.info("BossWalk idle: path is not , Boss on path");
    }

    /**
     * To get the position of the Boss.
     * @param entity
     * @return
     */
    public Point entityPosition(Entity entity) {
        bossWalkLogger.info("BossWalk entityPosition");
        return ((PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException(
                                "PositionComponent")))
                .getPosition();
    }
}
