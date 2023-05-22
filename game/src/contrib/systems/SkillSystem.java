package contrib.systems;

import contrib.components.SkillComponent;

import core.Entity;
import core.System;

import java.util.function.Consumer;

public class SkillSystem extends System {
    public SkillSystem() {
        super(SkillComponent.class);
    }

    /** reduces the cool down for all skills */
    @Override
    public void execute() {
        getEntityStream().forEach(reduceAllCoolDowns);
    }
    // Oder wenigstens Refactoring:

    // und irgendwo als in der jeweiligen Systemklasse:
    private static final Consumer<Entity> reduceAllCoolDowns =
            e -> ((SkillComponent) e.getComponent(SkillComponent.class).get()).reduceAllCoolDowns();
}
