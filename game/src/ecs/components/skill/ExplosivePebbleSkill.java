package ecs.components.skill;

import ecs.components.skill.ITargetSelection;
import ecs.damage.*;
import tools.Point;

public class ExplosivePebbleSkill extends ExplosiveProjectileSkill {

    public ExplosivePebbleSkill(ITargetSelection targetSelection) {
        super(
                "skills/bomb/bomb_down",
                0.5f,
                new Damage(10, DamageType.PHYSICAL, null),
                new Point(10, 10),
                targetSelection,
                5f);
    }

}
