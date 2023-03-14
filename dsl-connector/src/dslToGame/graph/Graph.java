package dslToGame.graph;

import java.util.ArrayList;
import java.util.Iterator;

public class Graph<T> {
    private final ArrayList<Edge> edges;
    private final ArrayList<Node<T>> nodes;

    public Iterator<Edge> getEdgeIterator() {
        return edges.iterator();
    }

    public Iterator<Node<T>> getNodeIterator() {
        return nodes.iterator();
    }

    public Graph(ArrayList<Edge> edges, ArrayList<Node<T>> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }
}
