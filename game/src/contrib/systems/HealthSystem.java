package contrib.systems;

import contrib.components.HealthComponent;
import contrib.components.StatsComponent;
import contrib.components.XPComponent;
import contrib.utils.components.draw.AdditionalAnimations;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.utils.components.MissingComponentException;

import java.util.stream.Stream;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the death of an entity when the health-points have fallen below 0.
 */
public final class HealthSystem extends System {

    public HealthSystem() {
        super(HealthComponent.class, DrawComponent.class);
    }

    @Override
    public void execute() {
        getEntityStream()
                // Consider only entities that have a HealthComponent
                // Form triples (e, hc, ac)
                .map(this::buildDataObject)
                // Apply damage
                .map(this::applyDamage)
                // Filter all dead entities
                .filter(hsd -> hsd.hc.isDead())
                .filter(
                        hsd -> {
                            if (!hsd.ac.hasAnimation(AdditionalAnimations.DIE)
                                    || hsd.ac
                                            .getAnimation(AdditionalAnimations.DIE)
                                            .get()
                                            .isLooping()) return true;
                            if (!hsd.ac.isCurrentAnimation(AdditionalAnimations.DIE)) {
                                hsd.ac.setCurrentAnimation(AdditionalAnimations.DIE);
                            }
                            return hsd.ac.getCurrentAnimation().isFinished();
                        })
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

    private HSData buildDataObject(Entity entity) {

        HealthComponent hc =
                entity.fetch(HealthComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, HealthComponent.class));
        DrawComponent ac =
                entity.fetch(DrawComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(entity, DrawComponent.class));
        return new HSData(entity, hc, ac);
    }

    private HSData applyDamage(HSData hsd) {
        hsd.e
                .fetch(StatsComponent.class)
                .ifPresentOrElse(
                        sc -> doDamageAndAnimation(hsd, calculateDamageWithMultipliers(sc, hsd)),
                        () ->
                                doDamageAndAnimation(
                                        hsd,
                                        Stream.of(DamageType.values())
                                                .mapToInt(hsd.hc::getDamage)
                                                .sum()));
        return hsd;
    }

    /**
     * Calculates damage with multipliers of the StatsComponent.
     *
     * @param statsComponent The StatsComponent of the entity.
     * @param hsd The HealthSystemData object.
     */
    private int calculateDamageWithMultipliers(StatsComponent statsComponent, HSData hsd) {
        return Stream.of(DamageType.values())
                .mapToInt(
                        dt ->
                                Math.round(
                                        statsComponent.getDamageModifiers().getMultiplier(dt)
                                                * hsd.hc.getDamage(dt)))
                .sum();
    }

    private void doDamageAndAnimation(HSData hsd, int dmgAmount) {
        if (dmgAmount > 0) {
            // we have some damage - let's show a little dance
            hsd.ac.setCurrentAnimation(AdditionalAnimations.HIT);
        }
        // reset all damage objects in health component and apply damage
        hsd.hc.clearDamage();
        hsd.hc.setCurrentHealthpoints(hsd.hc.getCurrentHealthpoints() - dmgAmount);
    }

    private void removeDeadEntities(HSData hsd) {
        // Entity appears to be dead, so let's clean up the mess
        hsd.hc.triggerOnDeath();
        hsd.ac.setCurrentAnimation(AdditionalAnimations.DIE);
        Game.removeEntity(hsd.hc.getEntity());

        // Add XP
        hsd.e
                .fetch(XPComponent.class)
                .ifPresent(
                        component ->
                                hsd.hc
                                        .getLastDamageCause()
                                        .flatMap(entity -> entity.fetch(XPComponent.class))
                                        .ifPresent(c -> c.addXP(component.getLootXP())));
    }

    // private record to hold all data during streaming
    private record HSData(Entity e, HealthComponent hc, DrawComponent ac) {}
}
