package graph;

import java.util.ArrayList;
import java.util.Iterator;

public class GraphNode<T> {
    private final T value;

    public static GraphNode<Void> NONE = new GraphNode<>(null);
    private final PropertyBag attributes = new PropertyBag();
    private final ArrayList<GraphEdge> edges = new ArrayList<>();

    /**
     * @return the attributes associated with this node
     */
    public PropertyBag getAttributes() {
        return attributes;
    }

    /**
     * @return an iterator for the edges in which this node is used
     */
    public Iterator<GraphEdge> edgeIterator() {
        return edges.iterator();
    }

    /**
     * @param edge the edge in which this node is use d
     * @return true, if adding succeeded, false otherwise
     */
    public boolean addEdge(GraphEdge edge) {
        return this.edges.add(edge);
    }

    /**
     * Constructor
     *
     * @param value the value to store in the node
     */
    public GraphNode(T value) {
        this.value = value;
    }

    /**
     * @return the value stored in this node
     */
    public T getValue() {
        return this.value;
    }
}
