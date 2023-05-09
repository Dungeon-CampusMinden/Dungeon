package ecs.components.skill;

import ecs.components.skill.ITargetSelection;
import ecs.components.skill.ReturningProjectileSkill;
import ecs.damage.*;
import tools.Point;


public class RubberballSkill extends ReturningProjectileSkill {

    public RubberballSkill(ITargetSelection targetSelection) {
        super(
                "skills/rubberball/rubberBall_Down/",
                0.5f,
                new Damage(10, DamageType.PHYSICAL, null),
                new Point(10, 10),
                targetSelection,
                5f);
    }
    
}
