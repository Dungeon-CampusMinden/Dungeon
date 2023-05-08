package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

import java.io.Serializable;

public class FireballSkill extends DamageProjectileSkill implements Serializable {
    public FireballSkill(ITargetSelection targetSelection) {
        super(
                "skills/fireball/fireBall_Down/",
                0.5f,
                new Damage(10, DamageType.FIRE, null),
                new Point(10, 10),
                targetSelection,
                5f);
    }
}
