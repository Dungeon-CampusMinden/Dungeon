package ecs.components.skill;

import ecs.damage.*;
import ecs.entities.Entity;
import tools.Point;

public class RubberballSkill extends ReturningProjectileSkill {

    public RubberballSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/rubberball/rubberBall_Down/",
                0.5f,
                new Damage(10, DamageType.PHYSICAL, null),
                new Point(10, 10),
                targetSelection,
                5f);
    }

}
