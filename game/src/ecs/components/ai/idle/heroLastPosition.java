package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Constants;

public class heroLastPosition implements IIdleAI {

    private GraphPath<Tile> path;
    private final int breakTime;
    private int currentBreak = 0;

    public heroLastPosition(int breakTime) {
        this.breakTime = breakTime * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            if (currentBreak >= breakTime) {
                currentBreak = 0;
                path = AITools.calculatePathToHero(entity);
                idle(entity);
            }

            currentBreak++;

        } else AITools.move(entity, path);
    }
}
