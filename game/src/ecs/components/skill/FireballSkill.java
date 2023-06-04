package ecs.components.skill;

import ecs.components.DamageComponent;
import ecs.components.stats.StatsComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import tools.Point;

public class FireballSkill extends DamageProjectileSkill {
    public FireballSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/fireball/fireBall_Down/",
                0.5f,
                new Damage((int) ((entity.getComponent(DamageComponent.class).isPresent()
                        ? entity.getComponent(DamageComponent.class).map(DamageComponent.class::cast).get()
                                .getDamage()
                        : 10)
                        * (entity.getComponent(StatsComponent.class).isPresent()
                                ? entity.getComponent(StatsComponent.class).map(StatsComponent.class::cast)
                                        .get().getDamageModifiers().getMultiplier(DamageType.FIRE)
                                : 1)),
                        DamageType.FIRE, entity),
                new Point(10, 10),
                targetSelection,
                5f);
    }
}
