package graph;

import core.Entity;

import task.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Node<T> implements Comparable<Node<T>> {
    private static int _idx;

    private final int idx;

    /**
     * @return The unique index of this node
     */
    public int getIdx() {
        return idx;
    }

    private final T value;

    public static Node<Void> NONE = new Node<>(null);

    // dot allows for the definition of attributes for each node, these will be stored in this
    // PropertyBag
    private final PropertyBag attributes = new PropertyBag();

    private final ArrayList<Edge> edges = new ArrayList<>();

    /**
     * @return the attributes associated with this node
     */
    public PropertyBag attributes() {
        return attributes;
    }

    /**
     * @return an iterator for the edges in which this node is used
     */
    public Iterator<Edge> iterator() {
        return edges.iterator();
    }

    /**
     * @param edge the edge in which this node is use d
     * @return true, if adding succeeded, false otherwise
     */
    public boolean addEdge(Edge edge) {
        return this.edges.add(edge);
    }

    /**
     * Constructor
     *
     * @param value the value to store in the node
     */
    public Node(T value) {
        this.idx = _idx++;
        this.value = value;
    }

    /**
     * @return the value stored in this node
     */
    public T value() {
        return this.value;
    }

    @Override
    public int compareTo(Node<T> o) {
        return this.idx - o.idx;
    }

    // TODO How to get the Entite Sets from the taskGraph?
    public Set<Set<Entity>> entities() {
        return new HashSet<>();
    }

    // TODO ??
    public Task task() {
        return null;
    }
}
