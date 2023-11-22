package contrib.systems;

import contrib.components.HealthComponent;
import contrib.utils.components.draw.AdditionalAnimations;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The HealthSystem offsets the damage to be done to all entities with the HealthComponent. Triggers
 * the death of an entity when the health-points have fallen below 0.
 *
 * <p>Entities with the {@link HealthComponent} and {@link DrawComponent} will be processed by this
 * system.
 */
public final class HealthSystem extends System {

    /** Create a new HealthSystem. */
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
     * <p>Returns a corresponding Boolean value if the entity can be removed from the game. This is
     * the case if the entity does not have a death animation, or it has already finished. Also, if
     * the entity has a death animation, and it is in loop mode, the entity will be marked for
     * removal.
     *
     * @param hsd HSData to check Animations in.
     * @return true if Entity can be removed from the game.
     */
    private boolean testDeathAnimationStatus(final HSData hsd) {
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

    private HSData activateDeathAnimation(final HSData hsd) {
        // set DeathAnimation as active animation
        Optional<Animation> deathAnimation = hsd.dc.animation(AdditionalAnimations.DIE);
        deathAnimation.ifPresent(
                animation -> hsd.dc.queueAnimation(animation.duration(), AdditionalAnimations.DIE));
        return hsd;
    }

    private HSData buildDataObject(final Entity entity) {

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

    private HSData applyDamage(final HSData hsd) {

        doDamageAndAnimation(
                hsd, Stream.of(DamageType.values()).mapToInt(hsd.hc::calculateDamageOf).sum());
        return hsd;
    }

    private void doDamageAndAnimation(final HSData hsd, final int dmgAmount) {
        if (dmgAmount > 0) {
            Optional<Animation> hitAnimation = hsd.dc.animation(AdditionalAnimations.HIT);
            // we have some damage - let's show a little dance
            hitAnimation.ifPresent(
                    animation ->
                            hsd.dc.queueAnimation(animation.duration(), AdditionalAnimations.HIT));
        }
        // reset all damage objects in health component and apply damage
        hsd.hc.clearDamage();
        hsd.hc.currentHealthpoints(hsd.hc.currentHealthpoints() - dmgAmount);
    }

    private void removeDeadEntities(final HSData hsd) {
        // Entity appears to be dead, so let's clean up the mess
        hsd.hc.triggerOnDeath(hsd.e);
        Game.remove(hsd.e);
    }

    // private record to hold all data during streaming
    private record HSData(Entity e, HealthComponent hc, DrawComponent dc) {}
}
