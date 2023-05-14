package contrib.utils.componentUtils.aiComponent.transition;

import core.Entity;
import contrib.utils.componentUtils.aiComponent.AITools;
import contrib.utils.componentUtils.aiComponent.ITransition;

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
