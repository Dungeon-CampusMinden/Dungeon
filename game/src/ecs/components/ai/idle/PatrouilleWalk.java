package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import level.elements.tile.Tile;
import starter.Game;
import tools.Constants;
import tools.Point;

public class PatrouilleWalk implements IIdleAI {

    private static final Random random = new Random();

    public enum MODE {
        /** Walks to a random checkpoint. */
        RANDOM,

        /** Looping the same path over and over again. */
        LOOP,

        /** Walks the path forward and then backward. */
        BACK_AND_FORTH
    }

    private final List<Tile> checkpoints = new ArrayList<>();
    private final int numberCheckpoints;
    private final int pauseFrames;
    private final float radius;
    private final MODE mode;
    private GraphPath<Tile> currentPath;
    private boolean initialized = false;
    private boolean forward = true;
    private int frameCounter = -1;
    private int currentCheckpoint = 0;

    /**
     * Walks a random pattern in a radius around the entity. The checkpoints will be chosen randomly
     * at first idle. After being initialized the checkpoints won't change anymore, only the order
     * may be.
     *
     * @param radius Max distance from the entity to walk
     * @param numberCheckpoints Number of checkpoints to walk to
     * @param pauseTime Max time in milliseconds to wait on a checkpoint. The actual time is a
     *     random number between 0 and this value
     */
    public PatrouilleWalk(float radius, int numberCheckpoints, int pauseTime, MODE mode) {
        this.radius = radius;
        this.numberCheckpoints = numberCheckpoints;
        this.pauseFrames = pauseTime / (1000 / Constants.FRAME_RATE);
        this.mode = mode;
    }

    private void init(Entity entity) {
        initialized = true;
        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        Point center = position.getPosition();
        Tile tile = Game.currentLevel.getTileAt(position.getPosition().toCoordinate());

        if (tile == null) {
            return;
        }

        List<Tile> accessibleTiles = AITools.getAccessibleTilesInRange(center, radius);

        if (accessibleTiles.isEmpty()) {
            return;
        }

        int maxTries = 0;
        while (this.checkpoints.size() < numberCheckpoints
                || accessibleTiles.size() == this.checkpoints.size()
                || maxTries >= 1000) {
            Tile t = accessibleTiles.get(random.nextInt(accessibleTiles.size()));
            if (!this.checkpoints.contains(t)) {
                this.checkpoints.add(t);
            }
            maxTries++;
        }
    }

    @Override
    public void idle(Entity entity) {
        if (!initialized) this.init(entity);

        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));

        if (currentPath != null && !AITools.pathFinished(entity, currentPath)) {
            if (AITools.pathLeft(entity, currentPath)) {
                currentPath =
                        AITools.calculatePath(
                                position.getPosition(),
                                this.checkpoints.get(currentCheckpoint).getCoordinate().toPoint());
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

        // HERE: (Path to checkpoint finished + pause time over) OR currentPath = null
        this.frameCounter = -1;

        switch (mode) {
            case RANDOM -> {
                Random rnd = new Random();
                currentCheckpoint = rnd.nextInt(checkpoints.size());
                currentPath =
                        AITools.calculatePath(
                                position.getPosition(),
                                this.checkpoints.get(currentCheckpoint).getCoordinate().toPoint());
            }
            case LOOP -> {
                currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
                currentPath =
                        AITools.calculatePath(
                                position.getPosition(),
                                this.checkpoints.get(currentCheckpoint).getCoordinate().toPoint());
            }
            case BACK_AND_FORTH -> {
                if (forward) {
                    currentCheckpoint += 1;
                    if (currentCheckpoint == checkpoints.size()) {
                        forward = false;
                        currentCheckpoint = checkpoints.size() - 2;
                    }
                } else {
                    currentCheckpoint -= 1;
                    if (currentCheckpoint == -1) {
                        forward = true;
                        currentCheckpoint = 1;
                    }
                }
                currentPath =
                        AITools.calculatePath(
                                position.getPosition(),
                                this.checkpoints.get(currentCheckpoint).getCoordinate().toPoint());
            }
            default -> {}
        }
    }
}
