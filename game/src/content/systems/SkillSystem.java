package content.systems;

import api.Game;
import api.System;
import content.component.SkillComponent;

public class SkillSystem extends System {

    /** reduces the cool down for all skills */
    @Override
    public void update() {
        Game.getEntities().stream()
                // Consider only entities that have a SkillComponent
                .flatMap(e -> e.getComponent(SkillComponent.class).stream())
                .forEach(sc -> ((SkillComponent) sc).reduceAllCoolDowns());
    }
}
