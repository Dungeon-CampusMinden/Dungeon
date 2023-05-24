package ecs.components.ai.transition;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PlayableComponent;
import ecs.entities.Entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements an AI that protects a specific entity with a HealthComponent, if the hero dealt damage to it.
 *
 * <p> Entity will stay in fight mode once entered.
 */
public class ProtectOnAttack implements ITransition {

    private boolean isInFight = false;

    private final Set<Entity> toProtect = new HashSet<>();

    /**
     * Constructor for one entity to protect
     *
     * @param entity to protect
     */
    ProtectOnAttack(Entity entity) {
        if (entity.getComponent(HealthComponent.class).isEmpty()) {
            throw (new MissingComponentException("HealthComponent"));
        }

        this.toProtect.add(entity);
    }

    /**
     * Constructor for a list of entities to protect
     *
     * <p>Checks if HealthComponent isPresent and adds it to the list of entities to protect
     *
     * @param entities - Entities that are protected
     */
    ProtectOnAttack(Collection<Entity> entities) {
        entities.stream()
            .peek(e -> e.getComponent(HealthComponent.class).orElseThrow())
            .forEach(this.toProtect::add);
    }

    /**
     * If lastDamage cause of an entity to protect has a playableComponent switch to fight mode
     *
     * @param entity associated entity
     * @return True if entity is in fight mode, false if entity is not
     */
    @Override
    public boolean isInFightMode(Entity entity) {
        if (isInFight) return true;

        isInFight =
            toProtect.stream()
                .map(e -> (HealthComponent) e.getComponent(HealthComponent.class).get())
                .anyMatch(
                    e ->
                        e.getLastDamageCause()
                            .map(t -> t.getComponent(PlayableComponent.class))
                            .isPresent());

        return isInFight;
    }
}
