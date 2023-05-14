package ecs.components.skill;

import ecs.components.skill.DamageMeleeSkill;
import ecs.components.skill.ITargetSelection;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

public class StabSkill extends DamageMeleeSkill {

    public StabSkill(ITargetSelection targetSelection) {
        super(
                "skills/Stab",
                0.5f,
                new Damage(20, DamageType.PHYSICAL, null),
                new Point(10, 10),
                targetSelection);
    }

}
