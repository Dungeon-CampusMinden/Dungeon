package ecs.components.ai.fight;

import ecs.entities.Entity;

public interface IFightAI {

    /**
     * Implements the combat behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void fight(Entity entity);
}
