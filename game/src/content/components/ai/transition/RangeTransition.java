package content.components.ai.transition;

import content.utils.ai.ITransition;
import content.utils.ai.AITools;
import api.Entity;

public class RangeTransition implements ITransition {

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
