package content.utils.ai;

import api.Entity;

public interface IFightAI {

    /**
     * Implements the combat behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void fight(Entity entity);
}
