package ecs.components.ai.idle;

import ecs.entities.Entity;
import savegame.IFieldSerializing;

public interface IIdleAI extends IFieldSerializing {

    /**
     * Implements the idle behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void idle(Entity entity);
}
