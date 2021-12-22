package controller;

import java.util.LinkedHashSet;

public abstract class AbstractController<T> extends LinkedHashSet<T> {
    /** Updates all Elements that are registered at this controller */
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
