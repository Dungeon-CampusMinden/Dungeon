package ecs.components.ai.idle;

import ecs.entities.Entity;

public interface IIdleAI {

    /**
     * Implements the idle behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void idle(Entity entity);
}
