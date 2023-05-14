package core.utils;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class allows managing a Collection inside the System-Loops.
 *
 * <p>Since the Systems iterate over a Set of Entity's, a {@link ConcurrentModificationException} is
 * normally thrown if this System adds/removes an entity of this set.
 *
 * <p>This class implements three different HashSets to allow delayed manipulation of the base Set.
 *
 * <p>{@link #current} contains the current "active" object of the collection. Use this Set to
 * iterate over and work with. Use {@link #getSet()} to get this Set.
 *
 * <p>Use {@link #add} to add the given object to {@link #toAdd}. After the call of {@link #update},
 * the objects inside this inner set will be added to {@link #current}
 *
 * <p>Use {@link #remove} to add the given object to {@link #toRemove}. After the call of {@link
 * #update}, the objects inside this inner set will be removed from {@link #current}
 *
 * <p>Use {@link #update()} to add/remove the objects to/from {@link #current}. If you do this
 * inside of an iteration over this Set, a {@link ConcurrentModificationException} will be thrown.
 * It is best to use this right before or right after iterating over the set.
 *
 * @param <T> Type of the Elements to store in the Sets.
 * @see ConcurrentModificationException
 * @see HashSet
 */
public final class DelayedSet<T> {

    private final Set<T> current = new HashSet<>();
    private final Set<T> toAdd = new HashSet<>();
    private final Set<T> toRemove = new HashSet<>();

    /**
     * Update the {@link #current} based on the elements in {@link #toAdd} and {@link #toRemove}.
     *
     * <p>Add all objects from {@link #toAdd} to {@link #current}. Remove all objects from {@link
     * #toRemove} to {@link #current}. Clears {@link #toAdd} and {@link #toAdd}
     *
     * <p>Note: First all elements from {@link #toAdd} will be added and than all elements from
     * {@link #toRemove} will be removed.
     */
    public void update() {
        current.addAll(toAdd);
        current.removeAll(toRemove);
        toAdd.clear();
        toRemove.clear();
    }

    /**
     * Add the given to {@link #toAdd}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be added to
     * {@link #current}
     *
     * @param t Object to add
     */
    public void add(T t) {
        toAdd.add(t);
    }

    /**
     * Add all objects of the given collection to {@link #toAdd}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be added to
     * {@link #current}
     *
     * @param collection contains all objects to add
     */
    public void addAll(Collection<T> collection) {
        toAdd.addAll(collection);
    }

    /**
     * Add the given to {@link #toRemove}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be removed from
     * {@link #current}
     *
     * @param t Object to remove
     */
    public void remove(T t) {
        toRemove.add(t);
    }

    /**
     * Add all objects of the given collection to {@link #toRemove}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be removed from
     * {@link #current}
     *
     * @param collection contains all objects to remove
     */
    public void removeAll(Collection<T> collection) {
        toRemove.addAll(collection);
    }

    /**
     * @return A copy of {@link #current} with all currently active elements
     */
    public Set<T> getSet() {
        return new HashSet<>(current);
    }

    /** Clear all internal Sets. */
    public void clear() {
        toAdd.clear();
        toRemove.clear();
        current.clear();
    }
}
