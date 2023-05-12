package ecs.components.ai.transition;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PlayableComponent;
import ecs.entities.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets AI protect an entity
 *
 * <p>Implements an AI that protects a specific entity with a HealthComponent if the hero afflicting damage to it.
 * Entity will stay in fight mode</p>
 */
public class ProtectOnAttack implements ITransition {

    private boolean isInfight = false;

    private final List<Entity> toProtect = new ArrayList<>();

    /**
     * Constructor for one entity to protect
     *
     * @param entity
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
     * <p>Checks if HealthComponent isPresent and adds it to the list of entities to protect</p>
     *
     * @param entities - Entities that are protected
     */
    ProtectOnAttack(List<Entity> entities) {
        entities.stream()
            .filter(e -> e.getComponent(HealthComponent.class).isPresent())
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
        if (isInfight) return true;

        isInfight = toProtect.stream()
            .map(e -> (HealthComponent) e.getComponent(HealthComponent.class).get())
            .anyMatch(e -> e.getLastDamageCause().map(t -> t.getComponent(PlayableComponent.class)).isPresent());

        return isInfight;
    }
}
