package ecs.components.skill;

import ecs.components.DamageComponent;
import ecs.components.stats.StatsComponent;
import ecs.damage.*;
import ecs.entities.Entity;
import tools.Point;

public class RubberballSkill extends ReturningProjectileSkill {

    public RubberballSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/rubberball/rubberBall_Down/",
                0.5f,
                new Damage((int) ((entity.getComponent(DamageComponent.class).isPresent()
                        ? 0.5 * entity.getComponent(DamageComponent.class).map(DamageComponent.class::cast).get()
                                .getDamage()
                        : 5)
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
