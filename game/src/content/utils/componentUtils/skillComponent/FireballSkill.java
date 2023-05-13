package content.utils.componentUtils.skillComponent;

import api.utils.Point;
import content.utils.componentUtils.healthComponent.Damage;
import content.utils.componentUtils.healthComponent.DamageType;

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
