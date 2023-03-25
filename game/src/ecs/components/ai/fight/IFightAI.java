package ecs.components.ai.fight;

import ecs.entities.Entity;
import savegame.IFieldSerializing;

public interface IFightAI extends IFieldSerializing {

    /**
     * Implements the combat behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void fight(Entity entity);
}
