package core.utils;

import java.util.*;
import java.util.stream.Stream;

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
public class DelayedSet<T> {

    protected final Set<T> current = new HashSet<>();
    protected final Set<T> toAdd = new HashSet<>();
    protected final Set<T> toRemove = new HashSet<>();

    /**
     * Update the {@link #current} based on the elements in {@link #toAdd} and {@link #toRemove}.
     *
     * <p>Add all objects from {@link #toAdd} to {@link #current} and remove all objects from
     * {@link#toRemove} to {@link #current}. Clears {@link #toRemove} and {@link #toAdd} .
     *
     * <p>Note: First all elements from {@link #toAdd} will be added and then all elements from
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
     * @return true if this set did not already contain the specified element
     */
    public boolean add(T t) {
        if (current.contains(t)) return false;
        return toAdd.add(t);
    }

    /**
     * Add all objects of the given collection to {@link #toAdd}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be added to
     * {@link #current}
     *
     * @param collection contains all objects to add
     * @return true if this set changed as a result of the call
     */
    public boolean addAll(Collection<T> collection) {
        if (current.containsAll(collection)) return false;
        return toAdd.addAll(collection);
    }

    /**
     * Add the given to {@link #toRemove}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be removed from
     * {@link #current}
     *
     * @param t Object to remove
     * @return true if this set changed as a result of the call
     */
    public boolean remove(T t) {
        if (current.contains(t) || toAdd.contains(t)) return toRemove.add(t);
        else return false;
    }

    /**
     * Add all objects of the given collection to {@link #toRemove}.
     *
     * <p>After the call of {@link #update}, the objects inside this inner set will be removed from
     * {@link #current}
     *
     * @param collection contains all objects to remove
     * @return true if this set changed as a result of the call
     */
    public boolean removeAll(Collection<T> collection) {
        if (Collections.disjoint(collection, current) && Collections.disjoint(collection, toAdd)) {
            return false;
        }
        return toRemove.addAll(collection);
    }

    /**
     * @return {@link #current} as stream
     */
    public Stream<T> getSetAsStream() {
        return current.stream();
    }

    /**
     * Add all Objects in {@link #current} to {@link #toRemove}
     *
     * <p>Will immediately clear all internal sets.
     */
    public void clear() {
        toAdd.clear();
        toRemove.clear();
        current.clear();
    }
}
