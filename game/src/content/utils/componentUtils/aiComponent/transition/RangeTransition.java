package content.utils.componentUtils.aiComponent.transition;

import api.Entity;
import content.utils.componentUtils.aiComponent.AITools;
import content.utils.componentUtils.aiComponent.ITransition;

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
