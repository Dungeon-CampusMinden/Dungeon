package ecs.components.ai.idle;

import ecs.entities.Entity;
import savegame.ISerializable;

public interface IIdleAI extends ISerializable {

    /**
     * Implements the idle behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void idle(Entity entity);
}
