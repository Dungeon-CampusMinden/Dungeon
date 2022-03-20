package controller;

import java.util.LinkedHashSet;

/**
 * A controller is a LinkedHashSet and manages elements of a specific type.
 *
 * @param <T> Type of elements to manage.
 */
public abstract class AbstractController<T> extends LinkedHashSet<T> {
    /** Updates all elements that are registered at this controller */
    public abstract void update();

    @Override
    public boolean contains(Object o) {
        assert (o != null);
        return super.contains(o);
    }

    @Override
    public boolean add(T t) {
        assert (t != null);
        return super.add(t);
    }

    @Override
    public boolean remove(Object o) {
        assert (o != null);
        return super.remove(o);
    }
}
