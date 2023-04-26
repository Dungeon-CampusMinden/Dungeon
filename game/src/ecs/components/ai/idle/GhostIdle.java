package ecs.components.ai.idle;

import com.badlogic.gdx.math.Vector2;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Constants;

public class GhostIdle implements IIdleAI {
    private final float followRadius;
    private final float maxDistance;
    private final int breakTime;
    private int currentBreak = 0;

    public GhostIdle(float followRadius, float maxDistance, int breakTimeInSeconds) {
        this.followRadius = followRadius;
        this.maxDistance = maxDistance;
        this.breakTime = breakTimeInSeconds * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity ghostEntity) {
        if (AITools.playerInRange(ghostEntity, maxDistance)) {
            if (!AITools.playerInRange(ghostEntity, followRadius)) {
                AITools.move(ghostEntity, AITools.calculatePathToHero(ghostEntity));
            } else {
                currentBreak++;

                if (currentBreak >= breakTime) {
                    currentBreak = 0;
                    AITools.move(ghostEntity, AITools.calculatePathToRandomTileInRange(ghostEntity, 1));
                }
            }
        } else {
            AITools.move(ghostEntity, AITools.calculatePathToRandomTileInRange(ghostEntity, 1));
        }
    }
}
