package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;

import core.Entity;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements an AI that protects a specific entity with a HealthComponent, if the hero dealt damage
 * to it.
 *
 * <p>Entity will stay in fight mode once entered.
 */
public class ProtectOnAttack implements Function<Entity, Boolean> {

    private boolean isInFight = false;

    private final Set<Entity> toProtect = new HashSet<>();

    /**
     * Constructor for one entity to protect
     *
     * @param entity to protect
     */
    public ProtectOnAttack(final Entity entity) {
        if (!entity.isPresent(HealthComponent.class)) {
            throw (new MissingComponentException("HealthComponent"));
        }

        toProtect.add(entity);
    }

    /**
     * Constructor for a list of entities to protect
     *
     * <p>Checks if HealthComponent isPresent and adds it to the list of entities to protect
     *
     * @param entities - Entities that are protected
     */
    public ProtectOnAttack(final Collection<Entity> entities) {
        // throw an exception for every entity that does not have a HealthComponent
        entities.stream()
                .filter(e -> e.fetch(HealthComponent.class).isEmpty())
                .map(e -> MissingComponentException.build(e, HealthComponent.class))
                .forEach(
                        mce -> {
                            throw mce;
                        });

        // collect every entity with a HealthComponent to add to the protection list
        List<Entity> toAdd = entities.stream()
                .filter(e -> e.fetch(HealthComponent.class).isPresent())
                    .toList();

        toProtect.addAll(toAdd);
    }

    /**
     * If lastDamage cause of an entity to protect has a playableComponent switch to fight mode
     *
     * @param entity associated entity
     * @return True if entity is in fight mode, false if entity is not
     */
    @Override
    public Boolean apply(final Entity entity) {
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
}
