package graph;

import java.util.ArrayList;
import java.util.Iterator;

public class GraphNode<T> {
    private final T value;

    public static GraphNode<Void> NONE = new GraphNode<>(null);
    private PropertyBag attributes = new PropertyBag();
    private ArrayList<GraphEdge> edges = new ArrayList<>();

    public PropertyBag getAttributes() {
        return attributes;
    }

    public Iterator<GraphEdge> edgeIterator() {
        return edges.iterator();
    }

    public boolean addEdge(GraphEdge edge) {
        return this.edges.add(edge);
    }

    public GraphNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }
}
