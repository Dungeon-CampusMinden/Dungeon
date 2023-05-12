package api.ecs.components.ai.idle;

import api.ecs.entities.Entity;

public interface IIdleAI {

    /**
     * Implements the idle behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void idle(Entity entity);
}
