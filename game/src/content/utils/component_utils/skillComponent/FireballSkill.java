package content.utils.component_utils.skillComponent;

import api.utils.Point;
import api.utils.component_utils.healthComponent.Damage;
import api.utils.component_utils.healthComponent.DamageType;
import api.utils.component_utils.skillComponent.ITargetSelection;

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
