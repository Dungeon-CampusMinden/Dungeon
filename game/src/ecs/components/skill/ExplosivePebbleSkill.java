package ecs.components.skill;

import ecs.components.DamageComponent;
import ecs.components.stats.StatsComponent;
import ecs.damage.*;
import ecs.entities.Entity;
import tools.Point;

public class ExplosivePebbleSkill extends ExplosiveProjectileSkill {

    public ExplosivePebbleSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/bomb/bomb_down",
                0.5f,
                new Damage((int) ((entity.getComponent(DamageComponent.class).isPresent()
                        ? 0.4 * entity.getComponent(DamageComponent.class).map(DamageComponent.class::cast).get()
                                .getDamage()
                        : 4)
                        * (entity.getComponent(StatsComponent.class).isPresent()
                                ? entity.getComponent(StatsComponent.class).map(StatsComponent.class::cast)
                                        .get().getDamageModifiers().getMultiplier(DamageType.PHYSICAL)
                                : 1)),
                        DamageType.PHYSICAL, entity),
                new Point(10, 10),
                targetSelection,
                5f);
    }

}
