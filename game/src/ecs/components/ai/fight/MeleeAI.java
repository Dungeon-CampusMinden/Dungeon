package ecs.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.entities.Entity;
import java.lang.reflect.InvocationTargetException;
import level.elements.tile.Tile;
import mydungeon.ECS;
import tools.Constants;

public class MeleeAI implements IFightAI {
    private float attackRange;
    private final int delay = Constants.FRAME_RATE;
    private int timeSinceLastUpdate = 0;
    private Skill fightSkill;
    private GraphPath<Tile> path;

    public MeleeAI(float attackRange, Skill fightSkill) {
        this.attackRange = attackRange;
        this.fightSkill = fightSkill;
    }

    @Override
    public void fight(Entity entity) {
        if (AITools.inRange(entity, attackRange)) {
            try {
                fightSkill.execute(entity);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (timeSinceLastUpdate >= delay) {
                path = AITools.calculateNewPath(entity, ECS.hero);
                timeSinceLastUpdate = -1;
            }
            timeSinceLastUpdate++;
            AITools.move(entity, path);
        }
    }
}
