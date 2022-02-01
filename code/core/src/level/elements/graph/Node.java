package level.elements.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in a graph.
 *
 * @author Andre Matutat
 */
public class Node {
    private final List<Integer> neighbours = new ArrayList<>();
    private int index;

    /**
     * Creates a new node.
     *
     * @param index The index of the node in the list of nodes of the graph it belongs to.
     */
    public Node(int index) {
        this.index = index;
    }

    /**
     * Copy a node without the neighbours.
     *
     * @param toCopy The node to copy.
     */
    public Node(Node toCopy) {
        this.setIndex(toCopy.getIndex());
    }

    /**
     * Add a new neighbour.
     *
     * @param neighbour The neighbour
     */
    public void connect(Node neighbour) {
        neighbours.add(neighbour.index);
    }

    /**
     * check if this node is not connected to specific another node
     *
     * @param node Node to check for.
     * @return if this node is not connected with the given node.
     */
    public boolean notConnectedWith(Node node) {
        return !neighbours.contains(node.getIndex());
    }

    public List<Integer> getNeighbours() {
        return neighbours;
    }

    /**
     * If two nodes have the same index, they are a copy of another
     *
     * @return The index of the node
     */
    public int getIndex() {
        return index;
    }

    /**
     * If two nodes have the same index, they are a copy of another Sets the index.
     *
     * @param index The index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return This node and his edges in dot-notation. Only prints edges with nodes that have a
     *     bigger index than this node.
     */
    public String toDot() {
        String dot = "";
        for (Integer node : getNeighbours())
            if (getIndex() < node) dot += getIndex() + "->" + node + "\n";
        return dot;
    }
}
