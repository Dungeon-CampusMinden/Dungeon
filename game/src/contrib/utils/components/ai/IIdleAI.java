package contrib.utils.components.ai;

import core.Entity;

public interface IIdleAI {

    /**
     * Implements the idle behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void idle(Entity entity);
}
