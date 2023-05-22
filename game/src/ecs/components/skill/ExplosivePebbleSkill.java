package ecs.components.skill;

import ecs.components.skill.ITargetSelection;
import ecs.damage.*;
import ecs.entities.Entity;
import tools.Point;

public class ExplosivePebbleSkill extends ExplosiveProjectileSkill {

    public ExplosivePebbleSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/bomb/bomb_down",
                0.5f,
                new Damage(10, DamageType.PHYSICAL, entity),
                new Point(10, 10),
                targetSelection,
                5f);
    }

}
