package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;

import core.Entity;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;

import java.util.*;
import java.util.function.Function;

/**
 * Implements an AI that protects a specific entity with a HealthComponent, if the hero dealt damage
 * to it.
 *
 * <p>Entity will stay in fight mode once entered.
 */
public class ProtectOnAttack implements Function<Entity, Boolean> {
    boolean setup = true;
    private final Collection<Entity> setupProtection;
    private final Set<Entity> toProtect = new HashSet<>();
    private boolean isInFight = false;

    /**
     * Constructor for one entity to protect
     *
     * @param entity to protect
     */
    public ProtectOnAttack(final Entity entity) {
        this(Set.of(entity));
    }

    /**
     * Constructor for a list of entities to protect
     *
     * <p>Checks if HealthComponent isPresent and adds it to the list of entities to protect
     *
     * @param entities - Entities that are protected
     */
    public ProtectOnAttack(final Collection<Entity> entities) {
        this.setupProtection = entities;
    }

    /**
     * If lastDamage cause of an entity to protect has a playableComponent switch to fight mode
     *
     * @param entity associated entity
     * @return True if entity is in fight mode, false if entity is not
     */
    @Override
    public Boolean apply(final Entity entity) {
        if (setup) doSetup();
        if (isInFight) return true;

        isInFight =
                toProtect.stream()
                        .map(
                                toProtect ->
                                        toProtect
                                                .fetch(HealthComponent.class)
                                                .orElseThrow(
                                                        () ->
                                                                MissingComponentException.build(
                                                                        toProtect,
                                                                        HealthComponent.class)))
                        .anyMatch(
                                toProtect ->
                                        toProtect
                                                .lastDamageCause()
                                                .map(
                                                        causeEntity ->
                                                                causeEntity.fetch(
                                                                        PlayerComponent.class))
                                                .isPresent());

        return isInFight;
    }

    private void doSetup() {
        // add every entity with a HealthComponent to the protection list
        // throw an exception for every entity that does not have a HealthComponent
        for (Entity entity : setupProtection) {
            if (entity.isPresent(HealthComponent.class)) toProtect.add(entity);
            else throw MissingComponentException.build(entity, HealthComponent.class);
        }
        setup = false;
    }
}
