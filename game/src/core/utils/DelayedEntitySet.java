package core.utils;

import core.Entity;
import core.Game;

import java.util.Set;

/**
 * A {@link DelayedSet} specifically for the handling of {@link Entity}.
 *
 * <p>After an Entity has been added to the active set, {@link Game#updateEntity} will be called so
 * the systems can check if they accept the entity.
 *
 * <p>After an Entity has been deleted from the active set, {@link Entity#dropAllComponents} will be
 * called.
 */
public final class DelayedEntitySet extends DelayedSet<Entity> {

    /**
     * Update the {@link #current} based on the elements in {@link #toAdd} and {@link #toRemove}.
     *
     * <p>Add all objects from {@link #toAdd} to {@link #current}. Remove all objects from {@link
     * #toRemove} to {@link #current}. Clears {@link #toAdd} and {@link #toAdd}
     *
     * <p>Note: First all elements from {@link #toAdd} will be added and than all elements from
     * {@link #toRemove} will be removed.
     *
     * <p>*
     *
     * <p>After an Entity has been added to the active set, {@link Game#updateEntity} will be called
     * so the systems can check if they accept the entity.
     *
     * <p>Before an Entity will be removed from the active set, {@link Entity#dropAllComponents}
     * will be called.
     */
    @Override
    public void update() {
        current.addAll(toAdd);
        toAdd.forEach(entity -> Game.updateEntity(entity));
        toRemove.forEach(entity -> entity.dropAllComponents());
        current.removeAll(toRemove);
        toAdd.clear();
        toRemove.clear();
    }

    /**
     * Clear all internal Sets. But keep the given Entities if they are in active (in {@link
     * #current)}
     *
     * <p>Before an Entity will be removed from the active set, {@link Entity#dropAllComponents}
     * will be called.
     *
     * @see DelayedSet
     * @param notClear Set with entities that shoult not be deletet, if the are in `current`.
     */
    public void clearExcept(final Set<Entity> notClear) {
        toRemove.addAll(current);
        toRemove.forEach(entity -> entity.dropAllComponents());
        toRemove.removeAll(notClear);
        toAdd.clear();
        toRemove.clear();
        current.clear();
    }

    public void clearExcept(Entity notClear) {
        toRemove.addAll(current);
        toRemove.remove(notClear);
        toRemove.forEach(entity -> entity.dropAllComponents());
        toAdd.clear();
        toRemove.clear();
        current.clear();
    }
}
