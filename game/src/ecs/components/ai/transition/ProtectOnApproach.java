package ecs.components.ai.transition;

import ecs.components.ai.AITools;
import ecs.entities.Entity;

/**
 * Implements an AI that protects one or more specific entities if hero is in the given range
 *
 * <p>Entity will stay in fight mode once it entered it</p>
 */
public class ProtectOnApproach implements ITransition {
    private final float range;

    private final Entity toProtect;

    private boolean isInFight = false;

    /**
     * Constructor needs a range and the entity to protect.
     *
     * @param range     - The range in which the entity should in fight mode
     * @param toProtect - The entity which should be protected
     */
    public ProtectOnApproach(float range, Entity toProtect) {
        this.range = range;
        this.toProtect = toProtect;
    }


    /**
     * If entity isn't in fight mode yet, check if player is in range of the entity to protect
     *
     * @param entity associated entity
     * @return Boolean
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
