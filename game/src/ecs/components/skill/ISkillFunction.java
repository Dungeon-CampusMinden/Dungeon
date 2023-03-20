package ecs.components.skill;

import ecs.entities.Entity;
import java.io.Serializable;

public interface ISkillFunction extends Serializable {

    /**
     * Implements the concrete skill of an entity
     *
     * @param entity which uses the skill
     */
    void execute(Entity entity);
}
