package contrib.systems;

import contrib.components.SkillComponent;

import core.Entity;
import core.System;

public class SkillSystem extends System {
    @Override
    public void accept(Entity entity) {
        if (entity.getComponent(SkillComponent.class).isPresent()) addEntity(entity);
        else removeEntity(entity);
    }
    /** reduces the cool down for all skills */
    @Override
    public void update() {
        getEntityStream()
                .forEach(
                        entity ->
                                ((SkillComponent) entity.getComponent(SkillComponent.class).get())
                                        .reduceAllCoolDowns());
    }
}
