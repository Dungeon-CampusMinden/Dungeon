package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Constants;
import tools.Point;

import java.util.Random;

public class WanderingWalk implements IIdleAI {

    private static final Random random = new Random();

    private final float radius;
    private final int pauseFrames;
    private final int wanderDistance;

    private GraphPath<Tile> currentPath;
    private int frameCounter = -1;

    public WanderingWalk(float radius, int wanderDistance, int pauseTime) {
        this.radius = radius;
        this.wanderDistance = wanderDistance;
        this.pauseFrames = pauseTime / (1000 / Constants.FRAME_RATE);
    }

    private Point getRandomWanderPoint(Point currentPosition) {
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        float distance = random.nextFloat() * wanderDistance;

        float newX = currentPosition.x + (float) Math.cos(angle) * distance;
        float newY = currentPosition.y + (float) Math.sin(angle) * distance;

        return new Point(newX, newY);
    }

    @Override
    public void idle(Entity entity) {
        PositionComponent position =
            (PositionComponent)
                entity.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));

        if (currentPath != null && !AITools.pathFinished(entity, currentPath)) {
            if (AITools.pathLeft(entity, currentPath)) {
                currentPath = AITools.calculatePath(position.getPosition(), getRandomWanderPoint(position.getPosition()));
            }
            if (currentPath.getCount() == 0) {
                return;
            }
            AITools.move(entity, currentPath);
            return;
        }

        if (currentPath != null && AITools.pathFinished(entity, currentPath)) {
            frameCounter = 0;
            currentPath = null;
            return;
        }

        if (frameCounter++ < pauseFrames && frameCounter != -1) {
            return;
        }

        frameCounter = -1;
        currentPath = AITools.calculatePath(position.getPosition(), getRandomWanderPoint(position.getPosition()));
    }
}
