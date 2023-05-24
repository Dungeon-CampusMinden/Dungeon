package contrib.utils.components.skill;

import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.utils.Point;

import java.util.function.Supplier;

public class FireballSkill extends DamageProjectile {
    public FireballSkill(Supplier<Point> targetSelection) {
        super(
                "skills/fireball/fireBall_Down/",
                0.5f,
                new Damage(1, DamageType.FIRE, null),
                new Point(1, 1),
                targetSelection,
                5f);
    }
}
