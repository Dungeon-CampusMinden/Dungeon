package controller;

import basiselements.DungeonElement;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A controller manages elements of a certain type and is based on a layer system.
 *
 * @param <T> Type of elements to manage.
 */
public abstract class AbstractController<T extends DungeonElement> extends LinkedHashSet<T>
        implements Iterable<T> {

    private final Map<ControllerLayer, List<T>> map = new TreeMap<>();
    private final Map<T, List<T>> map2 = new HashMap<>();

    /**
     * Updates all elements that are registered at this controller, removes deletable elements and
     * calls the update and draw method for every registered element.
     */
    public void update() {
        for (T e : this) {
            if (e.removable()) {
                remove(e);
            } else {
                e.update();
                e.draw();
            }
        }
    }

    /**
     * Returns true, if the element is in this controller.
     *
     * @param t Element to look for.
     * @return true, if the element is in this controller.
     */
    @Override
    public boolean contains(Object t) {
        assert (t != null);
        return map2.containsKey(t);
    }

    /**
     * Adds the element with default layer (20) to this controller, if it is not already added.
     *
     * @param t Element to add.
     * @return true, if this was successful.
     */
    @Override
    public boolean add(T t) {
        assert (t != null);
        return add(t, ControllerLayer.DEFAULT);
    }

    /**
     * Adds the element with the specific layer to this controller, if it is not already added.
     *
     * @param t Element to add.
     * @param layer Layer to add the element in.
     * @return true, if this was successful.
     */
    public boolean add(T t, ControllerLayer layer) {
        assert (t != null);
        if (contains(t)) {
            return false;
        }
        map.computeIfAbsent(layer, x -> new ArrayList<>()).add(t);
        map2.put(t, map.get(layer));
        return true;
    }

    /**
     * Removes the element from this controller, if it is in this controller.
     *
     * @param t Element to remove.
     * @return true, if this was successful.
     */
    @Override
    public boolean remove(Object t) {
        assert (t != null);
        if (!contains(t)) {
            return false;
        }
        map2.get(t).remove(t);
        map2.remove(t);
        return true;
    }

    /**
     * Tests, if this controller has no elements.
     *
     * @return true, if empty.
     */
    @Override
    public boolean isEmpty() {
        return map2.isEmpty();
    }

    /** Clears the entire controller (removes all elements). */
    @Override
    public void clear() {
        map.clear();
        map2.clear();
    }

    /** Removes all elements in the specific layer. */
    public void clearLayer(ControllerLayer layer) {
        for (T t : map.get(layer)) {
            map2.remove(t);
        }
        map.get(layer).clear();
    }

    /**
     * @return An unmodifiable iterator.
     */
    @Override
    public Iterator<T> iterator() {
        // creates a list copy of merged lists
        List<T> list = new ArrayList<>();
        for (List<T> l : map.values()) {
            list.addAll(l);
        }
        return list.iterator();
        // will not work with PowerMock:
        // return map.values().stream().flatMap(List::stream).toList().iterator();
    }

    // from HashSet
    @Override
    public int size() {
        return map2.size();
    }

    // from Iterable
    @Override
    public void forEach(Consumer<? super T> action) {
        iterator().forEachRemaining(action);
    }

    @Override
    public String toString() {
        return map2.keySet().toString();
    }

    // not needed methods
    // from LinkedHashSet, HashSet, AbstractSet, AbstractCollection and Collection
    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public Stream<T> stream() {
        throw new UnsupportedOperationException("please don't use this method");
    }

    @Override
    public Stream<T> parallelStream() {
        throw new UnsupportedOperationException("please don't use this method");
    }
}
