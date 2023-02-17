package ecs.components.skill;

import ecs.entities.Entity;

public interface ISkillFunction {

    /**
     * Implements one skill of an entity
     *
     * @param entity associated entity
     */
    void execute(Entity entity);
}
