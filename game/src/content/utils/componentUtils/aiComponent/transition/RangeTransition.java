package content.utils.componentUtils.aiComponent.transition;

import api.Entity;
import api.utils.componentUtils.aiComponent.ITransition;
import content.utils.componentUtils.aiComponent.AITools;

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
