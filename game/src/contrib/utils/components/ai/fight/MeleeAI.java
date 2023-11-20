package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.skill.Skill;

import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;

import java.util.function.Consumer;

public class MeleeAI implements Consumer<Entity> {
    private final float attackRange;
    private final int delay = Game.frameRate();
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
    public MeleeAI(final float attackRange, final Skill fightSkill) {
        this.attackRange = attackRange;
        this.fightSkill = fightSkill;
    }

    public MeleeAI(final float attackRange) {
        fightSkill = null;
        this.attackRange = attackRange;
    }

    @Override
    public void accept(final Entity entity) {
        if (LevelUtils.playerInRange(entity, attackRange)) {
            if (fightSkill != null) fightSkill.execute(entity);
        } else {
            if (path == null || timeSinceLastUpdate >= delay) {
                path = LevelUtils.calculatePathToHero(entity);
                timeSinceLastUpdate = -1;
            }
            timeSinceLastUpdate++;
            AIUtils.move(entity, path);
        }
    }
}
