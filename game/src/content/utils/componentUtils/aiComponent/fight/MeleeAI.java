package content.utils.componentUtils.aiComponent.fight;

import api.Entity;
import api.level.Tile;
import api.utils.Constants;
import api.utils.componentUtils.aiComponent.IFightAI;
import api.utils.componentUtils.skillComponent.Skill;
import com.badlogic.gdx.ai.pfa.GraphPath;
import content.utils.componentUtils.aiComponent.AITools;

public class MeleeAI implements IFightAI {
    private final float attackRange;
    private final int delay = Constants.FRAME_RATE;
    private int timeSinceLastUpdate = 0;
    private final Skill fightSkill;
    private GraphPath<Tile> path;

    /**
     * Attacks the player if he is within the given range. Otherwise, it will move towards the
     * player.
     *
     * @param attackRange Range in which the attack skill should be executed
     * @param fightSkill Skill to be used when an attack is performed
     */
    public MeleeAI(float attackRange, Skill fightSkill) {
        this.attackRange = attackRange;
        this.fightSkill = fightSkill;
    }

    @Override
    public void fight(Entity entity) {
        if (AITools.playerInRange(entity, attackRange)) {
            fightSkill.execute(entity);
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
