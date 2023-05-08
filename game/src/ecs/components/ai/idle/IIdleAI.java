package ecs.components.ai.idle;

import ecs.entities.Entity;

import java.io.Serializable;

public interface IIdleAI extends Serializable {

    /**
     * Implements the idle behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void idle(Entity entity);
}
