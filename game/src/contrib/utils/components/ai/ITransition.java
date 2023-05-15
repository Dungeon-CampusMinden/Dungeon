package contrib.utils.components.ai;

import core.Entity;

/** Determines when an ai switches between idle and fight */
public interface ITransition {

    /**
     * Function that determines whether an entity should be in combat mode
     *
     * @param entity associated entity
     * @return if the entity should fight
     */
    boolean isInFightMode(Entity entity);
}
