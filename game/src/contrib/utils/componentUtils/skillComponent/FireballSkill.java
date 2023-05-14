package contrib.utils.componentUtils.skillComponent;

import contrib.utils.componentUtils.healthComponent.Damage;
import contrib.utils.componentUtils.healthComponent.DamageType;
import core.utils.Point;

public class FireballSkill extends DamageProjectileSkill {
    public FireballSkill(ITargetSelection targetSelection) {
        super(
                "skills/fireball/fireBall_Down/",
                0.5f,
                new Damage(1, DamageType.FIRE, null),
                new Point(1, 1),
                targetSelection,
                5f);
    }
}
