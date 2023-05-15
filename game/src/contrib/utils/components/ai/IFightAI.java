package contrib.utils.components.ai;

import core.Entity;

public interface IFightAI {

    /**
     * Implements the combat behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void fight(Entity entity);
}
