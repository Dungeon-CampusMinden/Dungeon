package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.damage.Damage;
import ecs.entities.Entity;
import java.util.List;
import mydungeon.ECS;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the death of an entity when the hitpoints have fallen below 0.
 */
public class HealthSystem extends ECS_System {
    @Override
    public void update() {
        for (Entity entity : ECS.entities) {
            entity.getComponent(HealthComponent.name)
                    .ifPresent(
                            healthComponent -> {
                                {
                                    final AnimationComponent animationComponent =
                                            (AnimationComponent)
                                                    entity.getComponent(AnimationComponent.name)
                                                            .orElseThrow(
                                                                    () ->
                                                                            new MissingComponentException(
                                                                                    AnimationComponent
                                                                                            .name));

                                    HealthComponent hpComponent = (HealthComponent) healthComponent;

                                    // is the entity dead?
                                    if (hpComponent.getCurrentHitPoints() <= 0)
                                        letEntityDie(animationComponent, hpComponent);
                                    else hitEntity(animationComponent, hpComponent);
                                }
                            });
        }
    }

    private void hitEntity(AnimationComponent animationComponent, HealthComponent healthComponent) {
        // Entity is (still) alive - apply damage
        List<Damage> damageToGet = healthComponent.getDamageList();
        int dmgAmmount = 0;
        for (Damage dmg : damageToGet) {
            // todo: after we implemented Items like Armor: reduce
            // (or increase) the damage based on the stats and the
            // damage type
            switch (dmg.damageType()) {
                case PHYSICAL -> dmgAmmount += dmg.damageAmmount();
                case MAGIC -> dmgAmmount += dmg.damageAmmount();
                case FIRE -> dmgAmmount += dmg.damageAmmount();
            }
        }
        // if damage was caused, play getHitAnimation
        if (dmgAmmount > 0) {
            animationComponent.setCurrentAnimation(healthComponent.getGetHitAnimation());
        }

        healthComponent.clearDamageList();
        healthComponent.setCurrentHitPoints(healthComponent.getCurrentHitPoints() - dmgAmmount);
    }

    private void letEntityDie(
            AnimationComponent animationComponent, HealthComponent healthComponent) {
        // Entity appears to be dead, so let's clean up the mess
        healthComponent.triggerOnDeath();
        animationComponent.setCurrentAnimation(healthComponent.getDieAnimation());
        /*
        todo: Before removing the entity, check if the animation is finished
        Issue #246
        */
        ECS.entitiesToRemove.add(healthComponent.getEntity());
    }
}
