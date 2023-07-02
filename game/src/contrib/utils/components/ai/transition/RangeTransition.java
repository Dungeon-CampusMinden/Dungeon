package contrib.utils.components.ai.transition;

import contrib.utils.components.ai.AITools;

import core.Entity;

import java.util.function.Function;

public class RangeTransition implements Function<Entity, Boolean> {

    private final float range;

    /**
     * Switches to combat mode when the player is within range of the entity.
     *
     * @param range Range of the entity.
     */
    public RangeTransition(final float range) {
        this.range = range;
    }

    @Override
    public Boolean apply(final Entity entity) {
        return AITools.playerInRange(entity, range);
    }

    public float getRange() {
        return range;
    }
}
