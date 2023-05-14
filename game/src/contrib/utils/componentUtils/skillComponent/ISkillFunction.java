package contrib.utils.componentUtils.skillComponent;

import core.Entity;

public interface ISkillFunction {

    /**
     * Implements the concrete skill of an entity
     *
     * @param entity which uses the skill
     */
    void execute(Entity entity);
}
