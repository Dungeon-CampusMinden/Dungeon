package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AIUtils;
import contrib.utils.components.skill.Skill;

import core.Dungeon;
import core.Entity;
import core.level.Tile;
import core.level.utils.LevelUtils;

import java.util.function.Consumer;

public class MeleeAI implements Consumer<Entity> {
    private final float attackRange;
    private final int delay = Dungeon.frameRate();
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

    @Override
    public void accept(final Entity entity) {
        if (LevelUtils.playerInRange(entity, attackRange)) {
            fightSkill.execute(entity);
        } else {
            if (timeSinceLastUpdate >= delay) {
                path = LevelUtils.calculatePathToHero(entity);
                timeSinceLastUpdate = -1;
            }
            timeSinceLastUpdate++;
            AIUtils.move(entity, path);
        }
    }

    public float getAttackRange() {
        return attackRange;
    }

    public Skill getFightSkill() {
        return fightSkill;
    }
}
