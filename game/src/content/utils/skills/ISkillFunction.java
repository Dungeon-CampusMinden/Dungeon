package content.utils.skills;

import api.Entity;

public interface ISkillFunction {

    /**
     * Implements the concrete skill of an entity
     *
     * @param entity which uses the skill
     */
    void execute(Entity entity);
}
