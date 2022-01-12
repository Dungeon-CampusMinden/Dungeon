package level.elements;

import java.util.ArrayList;
import java.util.List;

/** @author Andre Matutat */
public class Node {
    private List<Integer> neighbours = new ArrayList<>();
    private int index;

    public Node(int index) {
        this.index = index;
    }

    /** copy node */
    public Node(Node n) {
        this.setIndex(n.getIndex());
    }

    /**
     * Add this node as neighbour
     *
     * @param n
     */
    public void connect(Node n) {
        neighbours.add(n.index);
    }

    /**
     * check if this node is not connected to specific another node
     *
     * @param n
     * @return
     */
    public boolean notConnectedWith(Node n) {
        if (!neighbours.contains(n.getIndex())) return true;
        else return false;
    }

    public List<Integer> getNeighbours() {
        return neighbours;
    }

    /**
     * If two nodes have the same index, they are a copy of another
     *
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * If two nodes have the same index, they are a copy of another Sets the index.
     *
     * @param i
     */
    public void setIndex(int i) {
        index = i;
    }

    public String toDot() {
        String dot = "";
        for (Integer n : getNeighbours()) if (getIndex() < n) dot += getIndex() + "->" + n + "\n";
        return dot;
    }
}
