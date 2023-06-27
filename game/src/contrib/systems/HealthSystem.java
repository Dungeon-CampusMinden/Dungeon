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

import java.util.function.Predicate;
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
        entityStream()
                // Consider only entities that have a HealthComponent
                // Form triples (e, hc, ac)
                .map(this::buildDataObject)
                // Apply damage
                .map(this::applyDamage)
                // Filter all dead entities
                .filter(hsd -> hsd.hc.isDead())
                // Set DeathAnimation if possible and not yet set
                .map(this::triggerDeathAnimation)
                .filter(this::filterByAnimation)
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

    /**
     * Filters the stream by checking the Animation of an Entity.
     * @param hsd HSData to check Animations in
     * @return true if Entity can be removed from the game
     */
    private boolean filterByAnimation(HSData hsd) {
        // test if hsd has a DeathAnimation
        Predicate<HSData> hasDeathAnimation =
                (hsData) -> hsData.ac.hasAnimation(AdditionalAnimations.DIE);
        // test if Animation is looping
        Predicate<HSData> isAnimationLooping =
                (hsData) -> hsData.ac.getAnimation(AdditionalAnimations.DIE).get().isLooping();
        // test if Animation has finished playing
        Predicate<HSData> isAnimationFinished =
                (hsData) -> hsData.ac.currentAnimation().isFinished();

        return !hasDeathAnimation.test(hsd)
                || isAnimationLooping.test(hsd)
                || isAnimationFinished.test(hsd);
    }

    private HSData triggerDeathAnimation(HSData hsd) {
        // if it has a DeathAnimation check if the DeathAnimation is active
        if (!hsd.ac.isCurrentAnimation(AdditionalAnimations.DIE)) {
            hsd.ac.currentAnimation(AdditionalAnimations.DIE);
        }

        return hsd;
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
                                                .mapToInt(hsd.hc::calculateDamageOf)
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
                                        statsComponent.damageModifiers().multiplierFor(dt)
                                                * hsd.hc.calculateDamageOf(dt)))
                .sum();
    }

    private void doDamageAndAnimation(HSData hsd, int dmgAmount) {
        if (dmgAmount > 0) {
            // we have some damage - let's show a little dance
            hsd.ac.currentAnimation(AdditionalAnimations.HIT);
        }
        // reset all damage objects in health component and apply damage
        hsd.hc.clearDamage();
        hsd.hc.currentHealthpoints(hsd.hc.currentHealthpoints() - dmgAmount);
    }

    private void removeDeadEntities(HSData hsd) {
        // Entity appears to be dead, so let's clean up the mess
        hsd.hc.triggerOnDeath();
        hsd.ac.currentAnimation(AdditionalAnimations.DIE);
        Game.removeEntity(hsd.hc.entity());

        // Add XP
        hsd.e
                .fetch(XPComponent.class)
                .ifPresent(
                        component ->
                                hsd.hc
                                        .lastDamageCause()
                                        .flatMap(entity -> entity.fetch(XPComponent.class))
                                        .ifPresent(c -> c.addXP(component.lootXP())));
    }

    // private record to hold all data during streaming
    private record HSData(Entity e, HealthComponent hc, DrawComponent ac) {}
}
