package ecs.components.skill;

import ecs.damage.*;
import ecs.entities.Entity;
import tools.Point;

public class PiercingArrowSkill extends PiercingProjectileSkill {

    public PiercingArrowSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/arrow/arrow_down",
                0.5f,
                new Damage(15, DamageType.PHYSICAL, entity),
                new Point(10, 10),
                targetSelection,
                5f);
    }

}
