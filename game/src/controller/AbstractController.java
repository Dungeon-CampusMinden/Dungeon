package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * A controller manages elements of a certain type and is based on a layer system.
 *
 * <p>Layer system means: All elements are listed in ascending order according to the layers in this
 * controller and in the order they are arranged in the layer.
 *
 * @param <T> generic type of elements to manage.
 */
public abstract class AbstractController<T> implements Iterable<T> {
    private final Map<ControllerLayer, List<T>> layerTreeMap = new TreeMap<>();
    private final Map<T, List<T>> elementHashMap = new HashMap<>();

    public abstract void process(T e);

    public void update() {
        for (T e : this) process(e);
    }

    /**
     * Returns true, if the element is in this controller.
     *
     * @param t Element to look for.
     * @return true, if the element is in this controller.
     */
    public boolean contains(Object t) {
        assert (t != null);
        return elementHashMap.containsKey(t);
    }

    /**
     * Adds the element with default layer (20) to this controller, if it is not already added.
     *
     * @param t Element to add.
     * @return true, if this was successful.
     */
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
        layerTreeMap.computeIfAbsent(layer, x -> new ArrayList<>()).add(t);
        elementHashMap.put(t, layerTreeMap.get(layer));
        return true;
    }

    /**
     * Adds all elements to the controller, if they are not already added.
     *
     * @param c Collection to add.
     * @return true, if this was modified.
     */
    public boolean addAll(Collection<T> c) {
        assert (c != null);
        boolean modified = false;
        for (T e : c) {
            modified |= add(e, ControllerLayer.DEFAULT);
        }
        return modified;
    }

    /**
     * Adds all elements with the specific layer to this controller, if they are not already added.
     *
     * @param c Collection to add.
     * @param layer Layer to add the element in.
     * @return true, if this was modified.
     */
    public boolean addAll(Collection<T> c, ControllerLayer layer) {
        assert (c != null);
        boolean modified = false;
        for (T e : c) {
            modified |= add(e, layer);
        }
        return modified;
    }

    /**
     * Removes the element from this controller, if it is in this controller.
     *
     * @param t Element to remove.
     * @return true, if this was successful.
     */
    public boolean remove(T t) {
        assert (t != null);
        if (!contains(t)) {
            return false;
        }
        elementHashMap.get(t).remove(t);
        elementHashMap.remove(t);
        return true;
    }

    /**
     * Tests, if this controller has no elements.
     *
     * @return true, if empty.
     */
    public boolean isEmpty() {
        return elementHashMap.isEmpty();
    }

    /** Clears the entire controller (removes all elements). */
    public void clear() {
        layerTreeMap.clear();
        elementHashMap.clear();
    }

    /** Removes all elements in the specific layer. */
    public void clearLayer(ControllerLayer layer) {
        for (T t : layerTreeMap.get(layer)) {
            elementHashMap.remove(t);
        }
        layerTreeMap.get(layer).clear();
    }

    /**
     * @return An ordered list of all elements in this controller.
     */
    private List<T> toList() {
        final List<T> list = new ArrayList<>();
        for (List<T> l : layerTreeMap.values()) {
            list.addAll(l);
        }
        return list;
    }

    /**
     * @return An iterator.
     */
    @Override
    public Iterator<T> iterator() {
        final List<T> list = toList();
        return new Iterator<T>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < list.size();
            }

            @Override
            public T next() {
                T e = list.get(i);
                i++;
                return e;
            }

            @Override
            public void remove() {
                i--;
                AbstractController.this.remove(list.get(i));
                list.remove(i);
            }
        };
    }

    public int size() {
        return elementHashMap.size();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        iterator().forEachRemaining(action);
    }

    @Override
    public String toString() {
        return toList().toString();
    }
}
