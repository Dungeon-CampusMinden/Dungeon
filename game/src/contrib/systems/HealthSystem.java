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
import core.utils.logging.CustomLogLevel;

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
                // Form triples (e, hc, dc)
                .map(this::buildDataObject)
                // Apply damage
                .map(this::applyDamage)
                // Filter all dead entities
                .filter(hsd -> hsd.hc.isDead())
                // Set DeathAnimation if possible and not yet set
                .map(this::activateDeathAnimation)
                // Filter by state of animation
                .filter(this::testDeathAnimationStatus)
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

    /**
     * Tests the existence and current status of the DeathAnimation of an entity.
     * 
     * <p>Returns a corresponding Boolean value if the entity can be removed from the game. This is the case if the entity does not have a death animation or it has already finished. Also, if the entity has a death animation and it is in loop mode, the entity is enabled for removal.
     *
     * @param hsd HSData to check Animations in
     * @return true if Entity can be removed from the game
     */
    private boolean testDeathAnimationStatus(HSData hsd) {
        DrawComponent dc = hsd.dc;
        // test if hsd has a DeathAnimation
        Predicate<DrawComponent> hasDeathAnimation =
                (drawComponent) -> drawComponent.hasAnimation(AdditionalAnimations.DIE);
        // test if Animation is looping
        Predicate<DrawComponent> isAnimationLooping = DrawComponent::isCurrentAnimationLooping;
        // test if Animation has finished playing
        Predicate<DrawComponent> isAnimationFinished = DrawComponent::isCurrentAnimationFinished;

        return !hasDeathAnimation.test(dc)
                || isAnimationLooping.test(dc)
                || isAnimationFinished.test(dc);
    }

    private HSData activateDeathAnimation(HSData hsd) {
        // set DeathAnimation as active animation
        if (!hsd.dc.isCurrentAnimation(AdditionalAnimations.DIE)) {
            hsd.dc.currentAnimation(AdditionalAnimations.DIE);
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
            hsd.dc.currentAnimation(AdditionalAnimations.HIT);
        }
        // reset all damage objects in health component and apply damage
        hsd.hc.clearDamage();
        hsd.hc.currentHealthpoints(hsd.hc.currentHealthpoints() - dmgAmount);
    }

    private void removeDeadEntities(HSData hsd) {
        // Entity appears to be dead, so let's clean up the mess
        hsd.hc.triggerOnDeath();
        hsd.dc.currentAnimation(AdditionalAnimations.DIE);
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
    private record HSData(Entity e, HealthComponent hc, DrawComponent dc) {}
}
