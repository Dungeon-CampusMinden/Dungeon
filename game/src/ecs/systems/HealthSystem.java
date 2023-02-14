package ecs.systems;

import static ecs.damage.DamageType.FIRE;
import static ecs.damage.DamageType.MAGIC;
import static ecs.damage.DamageType.PHYSICAL;

import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.entities.Entity;
import mydungeon.ECS;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the death of an entity when the hit-points have fallen below 0.
 */
public class HealthSystem extends ECS_System {

    // private record to hold all data during streaming
    private record HSData(Entity e, HealthComponent hc, AnimationComponent ac) {}

    @Override
    public void update() {
        ECS.entities.stream()
                // Consider only entities that have a HealthComponent
                .flatMap(e -> e.getComponent(HealthComponent.class).stream())
                // Form triples (e, hc, ac)
                .map(hc -> buildDataObject((HealthComponent) hc))
                // Apply damage
                .map(this::applyDamage)
                // Filter all dead entities
                .filter(hsd -> hsd.hc.getCurrentHitPoints() <= 0)
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

    private HSData buildDataObject(HealthComponent hc) {
        Entity e = hc.getEntity();

        AnimationComponent ac =
                (AnimationComponent)
                        e.getComponent(AnimationComponent.class).orElseThrow(this::missingAC);

        return new HSData(e, hc, ac);
    }

    private HSData applyDamage(HSData hsd) {
        int dmgAmount =
                hsd.hc.getDamage(PHYSICAL) + hsd.hc.getDamage(MAGIC) + hsd.hc.getDamage(FIRE);

        if (dmgAmount > 0) {
            // we have some damage - let's show a little dance
            hsd.ac.setCurrentAnimation(hsd.hc.getGetHitAnimation());
        }

        // reset all damage objects in health component and apply damage
        hsd.hc.clearDamageList();
        hsd.hc.setCurrentHitPoints(hsd.hc.getCurrentHitPoints() - dmgAmount);

        return hsd;
    }

    private void removeDeadEntities(HSData hsd) {
        // Entity appears to be dead, so let's clean up the mess
        hsd.hc.triggerOnDeath();
        hsd.ac.setCurrentAnimation(hsd.hc.getDieAnimation());
        // TODO: Before removing the entity, check if the animation is finished (Issue #246)
        ECS.entitiesToRemove.add(hsd.hc.getEntity());
    }

    private MissingComponentException missingAC() {
        return new MissingComponentException("AnimationComponent");
    }
}
