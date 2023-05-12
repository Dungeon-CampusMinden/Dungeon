package ecs.components.ai.transition;

import ecs.components.ai.AITools;
import ecs.entities.Entity;

/**
 * Implements an AI that protects an entity if hero is in the given range
 *
 * <p>Entity will stay in fight mode once it entered it
 */
public class ProtectOnApproach implements ITransition {
    private final float range;

    private final Entity toProtect;

    private boolean isInFight = false;

    /**
     * Constructor needs a range and the entity to protect.
     *
     * @param range - The range in which the entity should in fight mode
     * @param toProtect - The entity which should be protected
     */
    public ProtectOnApproach(float range, Entity toProtect) {
        this.range = range;
        this.toProtect = toProtect;
    }

    /**
     * If entity that protects isn't in fight mode yet, check if player is in range of the protected
     * entity
     *
     * @param entity associated entity
     * @return boolean
     */
    @Override
    public boolean isInFightMode(Entity entity) {
        if (isInFight) {
            return true;
        }

        isInFight = AITools.playerInRange(toProtect, range);

        return isInFight;
    }
}
