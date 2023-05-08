package ecs.components.ai.fight;

import ecs.entities.Entity;

import java.io.Serializable;

public interface IFightAI extends Serializable {

    /**
     * Implements the combat behavior of an AI controlled entity
     *
     * @param entity associated entity
     */
    void fight(Entity entity);
}
