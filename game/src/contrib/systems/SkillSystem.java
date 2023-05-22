package contrib.systems;

import contrib.components.SkillComponent;

import core.Entity;
import core.System;

public class SkillSystem extends System {
    @Override
    protected boolean accept(Entity entity) {
        if (entity.getComponent(SkillComponent.class).isPresent()) return true;
        return false;
    }
    /** reduces the cool down for all skills */
    @Override
    public void execute() {
        getEntityStream()
                .forEach(
                        entity ->
                                ((SkillComponent) entity.getComponent(SkillComponent.class).get())
                                        .reduceAllCoolDowns());
    }
}
