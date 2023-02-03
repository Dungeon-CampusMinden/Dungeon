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
                            component -> {
                                {
                                    final AnimationComponent ac =
                                            (AnimationComponent)
                                                    entity.getComponent(AnimationComponent.name)
                                                            .orElseThrow(
                                                                    () ->
                                                                            new MissingComponentException(
                                                                                    AnimationComponent
                                                                                            .name));

                                    HealthComponent hpComponent = (HealthComponent) component;

                                    // is the entity dead?
                                    if (hpComponent.getCurrentHitPoints() <= 0) {
                                        hpComponent.triggerOnDeath();
                                        ac.setCurrentAnimation(hpComponent.getDieAnimation());
                                        // todo: After animation is finished
                                        ECS.entitiesToRemove.add(entity);
                                    } else {
                                        // make damage
                                        List<Damage> damageToGet = hpComponent.getDamageList();
                                        int dmgAmmount = 0;
                                        for (Damage dmg : damageToGet) {
                                            // place to increase or decrease dmg based on skills,
                                            // items
                                            dmgAmmount += dmg.damageAmmount();
                                        }
                                        // if damage was caused, play getHitAnimation
                                        if (dmgAmmount > 0) {
                                            ac.setCurrentAnimation(
                                                    hpComponent.getGetHitAnimation());
                                        }
                                        // clear list
                                        damageToGet.clear();
                                        // set hit points
                                        hpComponent.setCurrentHitPoints(
                                                hpComponent.getCurrentHitPoints() - dmgAmmount);
                                    }
                                }
                            });
        }
    }
}
