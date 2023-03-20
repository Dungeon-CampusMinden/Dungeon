package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import java.io.Serializable;
import tools.Point;

public class FireballSkill extends DamageProjectileSkill implements Serializable {

    public static final long serialVersionUID = 1L;

    public FireballSkill(ITargetSelection targetSelection) {
        super(
                "skills/fireball/fireBall_Down/",
                0.5f,
                new Damage(1, DamageType.FIRE, null),
                new Point(10, 10),
                targetSelection,
                5f);
    }
}
