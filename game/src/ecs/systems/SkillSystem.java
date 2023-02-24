package ecs.systems;

import ecs.components.skill.SkillComponent;
import mydungeon.ECS;

public class SkillSystem extends ECS_System {

    /** reduces the cool down for all skills */
    @Override
    public void update() {
        ECS.entities.stream()
                // Consider only entities that have a SkillComponent
                .flatMap(e -> e.getComponent(SkillComponent.class).stream())
                .forEach(sc -> ((SkillComponent) sc).reduceAllCoolDowns());
    }
}
