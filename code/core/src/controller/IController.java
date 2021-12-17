package controller;

import java.util.List;
import java.util.Set;

public interface IController<T> {

    /** Updates all Elements that are registered at this controller */
    void update();

    /** Register an element. */
    void add(T element);

    /** Removes an element from the set. */
    void remove(T element);

    /** Removes all elements from the set. */
    void removeAll();

    /** Returns <code>true</code> if the element is registered. */
    boolean contains(T element);

    /** Returns a copy set of all elements. */
    Set<T> getSet();

    /** Returns a copy list of all elements. */
    List<T> getList();
}
