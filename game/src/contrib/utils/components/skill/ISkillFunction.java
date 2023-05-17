package contrib.utils.components.skill;

import core.Entity;

public interface ISkillFunction {

    /**
     * Implements the specific functionality of a skill.
     *
     * @param entity which uses the skill
     */
    void execute(Entity entity);
}
