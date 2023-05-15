package contrib.systems;

import contrib.components.HealthComponent;
import contrib.components.StatsComponent;
import contrib.components.XPComponent;
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
public class HealthSystem extends System {

    // private record to hold all data during streaming
    private record HSData(Entity e, HealthComponent hc, DrawComponent ac) {}

    @Override
    public void update() {
        Game.getEntities().stream()
                // Consider only entities that have a HealthComponent
                .flatMap(e -> e.getComponent(HealthComponent.class).stream())
                // Form triples (e, hc, ac)
                .map(hc -> buildDataObject((HealthComponent) hc))
                // Apply damage
                .map(this::applyDamage)
                // Filter all dead entities
                .filter(hsd -> hsd.hc.isDead())
                .filter(
                        hsd -> {
                            if (hsd.hc.getDeathAnimation() == null
                                    || hsd.hc.getDeathAnimation().isLooping()) return true;
                            if (!hsd.ac.getCurrentAnimation().equals(hsd.hc.getDeathAnimation())) {
                                hsd.ac.setCurrentAnimation(hsd.hc.getDeathAnimation());
                            }
                            return hsd.ac.getCurrentAnimation().isFinished();
                        })
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

    private HSData buildDataObject(HealthComponent hc) {
        Entity e = hc.getEntity();

        DrawComponent ac =
                (DrawComponent)
                        e.getComponent(DrawComponent.class).orElseThrow(HealthSystem::missingAC);

        return new HSData(e, hc, ac);
    }

    private HSData applyDamage(HSData hsd) {
        hsd.e
                .getComponent(StatsComponent.class)
                .ifPresentOrElse(
                        sc -> {
                            StatsComponent scomp = (StatsComponent) sc;
                            doDamageAndAnimation(hsd, calculateDamageWithMultipliers(scomp, hsd));
                        },
                        () -> {
                            doDamageAndAnimation(
                                    hsd,
                                    Stream.of(DamageType.values())
                                            .mapToInt(hsd.hc::getDamage)
                                            .sum());
                        });
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
            hsd.ac.setCurrentAnimation(hsd.hc.getGetHitAnimation());
        }
        // reset all damage objects in health component and apply damage
        hsd.hc.clearDamage();
        hsd.hc.setCurrentHealthpoints(hsd.hc.getCurrentHealthpoints() - dmgAmount);
    }

    private void removeDeadEntities(HSData hsd) {
        // Entity appears to be dead, so let's clean up the mess
        hsd.hc.triggerOnDeath();
        hsd.ac.setCurrentAnimation(hsd.hc.getDeathAnimation());
        Game.removeEntity(hsd.hc.getEntity());

        // Add XP
        hsd.e
                .getComponent(XPComponent.class)
                .ifPresent(
                        component -> {
                            XPComponent deadXPComponent = (XPComponent) component;
                            hsd.hc
                                    .getLastDamageCause()
                                    .flatMap(entity -> entity.getComponent(XPComponent.class))
                                    .ifPresent(
                                            c -> {
                                                XPComponent killerXPComponent = (XPComponent) c;
                                                killerXPComponent.addXP(
                                                        deadXPComponent.getLootXP());
                                            });
                        });
    }

    private static MissingComponentException missingAC() {
        return new MissingComponentException("AnimationComponent");
    }
}
