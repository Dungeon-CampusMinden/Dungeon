package core.utils;

import core.Entity;
import core.Game;

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
     * <p>After an Entity has been deleted from the active set, {@link Entity#dropAllComponents}
     * will be called.
     */
    @Override
    public void update() {
        current.addAll(toAdd);
        toAdd.forEach(entity -> Game.updateEntity(entity));
        current.removeAll(toRemove);
        toRemove.forEach(entity -> entity.dropAllComponents());
        toAdd.clear();
        toRemove.clear();
    }
}
