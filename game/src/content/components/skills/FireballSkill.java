package content.components.skills;

import content.utils.damage.Damage;
import content.utils.damage.DamageType;
import content.utils.skills.ITargetSelection;
import content.utils.position.Point;

public class FireballSkill extends DamageProjectileSkill {
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
