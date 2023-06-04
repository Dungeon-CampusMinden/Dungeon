package ecs.components.skill;

import ecs.components.DamageComponent;
import ecs.components.stats.StatsComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import tools.Point;

public class StabSkill extends DamageMeleeSkill {

    public StabSkill(ITargetSelection targetSelection, Entity entity) {
        super(
                "skills/Stab",
                0.5f,
                new Damage((int) ((entity.getComponent(DamageComponent.class).isPresent()
                        ? 2 * entity.getComponent(DamageComponent.class).map(DamageComponent.class::cast).get()
                                .getDamage()
                        : 20)
                        * (entity.getComponent(StatsComponent.class).isPresent()
                                ? entity.getComponent(StatsComponent.class).map(StatsComponent.class::cast)
                                        .get().getDamageModifiers().getMultiplier(DamageType.PHYSICAL)
                                : 1)),
                        DamageType.PHYSICAL, entity),
                new Point(10, 10),
                targetSelection);
    }

}
