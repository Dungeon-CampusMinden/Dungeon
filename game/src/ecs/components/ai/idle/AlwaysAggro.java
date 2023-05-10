package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Constants;

public class AlwaysAggro implements IIdleAI {
    private final float radius;
    private GraphPath<Tile> path;
    private final int breakTime;
    private int currentBreak = 0;
    private final Skill skill;
    private final int delay = Constants.FRAME_RATE;
    private int timeSinceLastUpdate = 0;

    /**
     * when in idle range, additionally to moving within a radius, the monster will use skills
     *
     * @param radius radius in which to move
     * @param breakTimeInSeconds time to wait between movements
     * @param skill skill to use while idling
     */
    public AlwaysAggro(float radius, int breakTimeInSeconds, Skill skill) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Constants.FRAME_RATE;
        this.skill = skill;
    }

    @Override
    public void idle (Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            if (currentBreak >= breakTime) {
                currentBreak = 0;
                path = AITools.calculatePathToRandomTileInRange(entity, radius);
                idle(entity);
            }

            currentBreak++;

        } else AITools.move(entity, path);
        fight(entity);
    }

    private void fight(Entity entity) {
        if (AITools.playerInRange(entity, radius)) {
            skill.execute(entity);
        } else {
            if (timeSinceLastUpdate >= delay) {
                path = AITools.calculatePathToHero(entity);
                timeSinceLastUpdate = -1;
            }
            timeSinceLastUpdate++;
            AITools.move(entity, path);
        }
    }
}
