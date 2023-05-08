package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

public class SwordSkill extends DamageProjectileSkill{
    public SwordSkill(ITargetSelection targetSelection) {
        super(
            "weapon/weapon_regular_sword.png",
            0.5f,
            new Damage(1, DamageType.PHYSICAL, null),
            new Point(10, 10),
            targetSelection,
            0.3f);
    }
}
