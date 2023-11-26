package contrib.utils.components.ai.transition;

import core.Entity;
import core.level.utils.LevelUtils;

import java.util.function.Function;

/**
 * Implementation of a transition between idle and fight mode. Activates fight mode when the hero is
 * within a specified range of the entity.
 */
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
        return LevelUtils.playerInRange(entity, range);
    }
}
