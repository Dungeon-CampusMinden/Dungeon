package ecs.components.ai.fight;

import ecs.entities.Entity;
import savegame.ISerializable;

public interface IFightAI extends ISerializable {

    /**
     * Implements the combat behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void fight(Entity entity);
}
