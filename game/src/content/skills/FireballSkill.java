package content.skills;

import component_tools.damage.Damage;
import component_tools.damage.DamageType;
import component_tools.skills.ITargetSelection;
import component_tools.position.Point;

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
