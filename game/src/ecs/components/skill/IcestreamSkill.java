package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

public class IcestreamSkill extends DamageProjectileSkill{
    public IcestreamSkill(ITargetSelection targetSelection) {
        super(
            "skills/icestream/",
            0.1f,
            new Damage(1, DamageType.ICE, null),
            new Point(10, 10),
            targetSelection,
            5f);
    }
}
