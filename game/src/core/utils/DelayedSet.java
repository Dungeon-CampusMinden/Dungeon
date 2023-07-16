package core.utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This class allows managing a collection inside the system loops.
 *
 * <p>Since the systems iterate over a set of entities, a {@link ConcurrentModificationException} is
 * normally thrown if the system adds or removes an entity from this set.
 *
 * <p>This class implements three different hash sets to enable delayed manipulation of the base
 * set.
 *
 * <p>{@link #current} contains the current "active" objects of the collection. Use this set to
 * iterate over and work with. Use {@link #stream()} to get this set as a stream.
 *
 * <p>Use {@link #add} to add the given object to {@link #toAdd}. After calling {@link #update()},
 * the objects inside this inner set will be added to {@link #current}.
 *
 * <p>Use {@link #remove} to add the given object to {@link #toRemove}. After calling {@link
 * #update()}, the objects inside this inner set will be removed from {@link #current}.
 *
 * <p>Use {@link #update()} to add or remove the objects to/from {@link #current}. If you do this
 * inside an iteration over this set, a {@link ConcurrentModificationException} will be thrown. It
 * is best to use this right before or right after iterating over the set.
 *
 * @param <T> Type of the elements to store in the sets.
 * @see ConcurrentModificationException
 * @see HashSet
 */
public final class DelayedSet<T> {

    private final Set<T> current = Collections.synchronizedSet(new HashSet<>());
    private final Set<T> toAdd = Collections.synchronizedSet(new HashSet<>());
    private final Set<T> toRemove = Collections.synchronizedSet(new HashSet<>());

    /**
     * Update the {@link #current} set based on the elements in {@link #toAdd} and {@link
     * #toRemove}.
     *
     * <p>Add all objects from {@link #toAdd} to {@link #current} and remove all objects from {@link
     * #toRemove} from {@link #current}. Clears {@link #toRemove} and {@link #toAdd}.
     *
     * <p>Note: First, all elements from {@link #toAdd} will be added, and then all elements from
     * {@link #toRemove} will be removed.
     */
    public void update() {
        synchronized (current) {
            synchronized (toAdd) {
                synchronized (toRemove) {
                    current.addAll(toAdd);
                    current.removeAll(toRemove);
                    toAdd.clear();
                    toRemove.clear();
                }
            }
        }
    }

    /**
     * Add the given object to {@link #toAdd}.
     *
     * <p>After calling {@link #update}, the objects inside this inner set will be added to {@link
     * #current}.
     *
     * @param t Object to add
     */
    public void add(T t) {
        toAdd.add(t);
    }

    /**
     * Add all objects from the given collection to {@link #toAdd}.
     *
     * <p>After calling {@link #update}, the objects inside this inner set will be added to {@link
     * #current}.
     *
     * @param collection Collection containing the objects to add
     */
    public void addAll(Collection<T> collection) {
        toAdd.addAll(collection);
    }

    /**
     * Add the given object to {@link #toRemove}.
     *
     * <p>After calling {@link #update}, the objects inside this inner set will be removed from
     * {@link #current}.
     *
     * @param t Object to remove
     */
    public void remove(T t) {
        toRemove.add(t);
    }

    /**
     * Add all objects of the given collection to {@link #toRemove}.
     *
     * <p>After calling {@link #update}, the objects inside this inner set will be removed from
     * {@link #current}.
     *
     * @param collection Contains all objects to remove
     */
    public void removeAll(Collection<T> collection) {
        toRemove.addAll(collection);
    }

    /**
     * @return {@link #current} as stream
     */
    public Stream<T> stream() {
        synchronized (current) {
            return new ArrayList<>(current).stream();
        }
    }

    /**
     * @return {@link #toAdd}
     */
    public Set<T> toAdd() {
        synchronized (toAdd) {
            return new HashSet<>(toAdd);
        }
    }

    /**
     * @return {@link #toRemove}
     */
    public Set<T> toRemove() {
        synchronized (toRemove) {
            return new HashSet<>(toRemove);
        }
    }

    /**
     * Execute the given function on each entity in the {@link #toAdd} set.
     *
     * @param function Function to execute on each entity in the {@link #toAdd} set.
     */
    public void foreachEntityInAddSet(Consumer<T> function) {
        synchronized (toAdd) {
            toAdd.forEach(function);
        }
    }

    /**
     * Execute the given function on each entity in the {@link #toRemove} set.
     *
     * @param function Function to execute on each entity in the {@link #toRemove} set.
     */
    public void foreachEntityInRemoveSet(Consumer<T> function) {
        synchronized (toRemove) {
            toRemove.forEach(function);
        }
    }

    /**
     * Add all objects in {@link #current} to {@link #toRemove}.
     *
     * <p>This method will immediately clear all internal sets.
     */
    public void clear() {
        synchronized (current) {
            synchronized (toAdd) {
                synchronized (toRemove) {
                    current.clear();
                    toAdd.clear();
                    toRemove.clear();
                }
            }
        }
    }
}
