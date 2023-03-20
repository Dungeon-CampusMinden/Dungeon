package ecs.components.ai.transition;

import ecs.components.ai.AITools;
import ecs.entities.Entity;
import savegame.IFieldSerializing;

public class RangeTransition implements ITransition, IFieldSerializing {

    private final float range;

    /**
     * Switches to combat mode when the player is within range of the entity.
     *
     * @param range Range of the entity.
     */
    public RangeTransition(float range) {
        this.range = range;
    }

    @Override
    public boolean isInFightMode(Entity entity) {
        return AITools.playerInRange(entity, range);
    }
}
