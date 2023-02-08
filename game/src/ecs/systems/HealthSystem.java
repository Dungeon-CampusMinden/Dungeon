package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import mydungeon.ECS;

import static ecs.damage.DamageType.FIRE;
import static ecs.damage.DamageType.MAGIC;
import static ecs.damage.DamageType.PHYSICAL;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the death of an entity when the hitpoints have fallen below 0.
 */
public class HealthSystem extends ECS_System {
    @Override
    public void update() {
        ECS.entities.stream()
            .flatMap(e -> e.getComponent(HealthComponent.class).stream())
            .forEach(hc -> applyDamage((HealthComponent) hc));
    }

    private void applyDamage(HealthComponent healthComponent) {
        AnimationComponent animationComponent = (AnimationComponent) healthComponent.getEntity()
            .getComponent(AnimationComponent.class)
            .orElseThrow(
                () -> new MissingComponentException("AnimationComponent")
            );

        if (healthComponent.getCurrentHitPoints() <= 0)
            // Entity appears to be dead, so let's clean up the mess
            letEntityDie(animationComponent, healthComponent);
        else
            // Entity is (still) alive - apply damage
            hitEntity(animationComponent, healthComponent);
    }

    private void hitEntity(AnimationComponent animationComponent, HealthComponent healthComponent) {
        // Entity is (still) alive - apply damage
        int dmgAmount = healthComponent.getDamage(PHYSICAL) + healthComponent.getDamage(MAGIC) + healthComponent.getDamage(FIRE);

        if (dmgAmount > 0) {
            // we have some damage - let's show a little dance
            animationComponent.setCurrentAnimation(healthComponent.getGetHitAnimation());
        }

        // reset all damage objects in health component and apply damage
        healthComponent.clearDamageList();
        healthComponent.setCurrentHitPoints(healthComponent.getCurrentHitPoints() - dmgAmount);
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
