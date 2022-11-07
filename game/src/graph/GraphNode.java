package graph;

import java.util.HashMap;

public class GraphNode<T extends Object> {
    private final T value;

    public static GraphNode<Void> NONE = new GraphNode<>(null);
    public HashMap<String, Object> attributes = new HashMap<>();

    public GraphNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }
}
