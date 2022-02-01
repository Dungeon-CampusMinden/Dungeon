package level.generator.dungeong.levelg;

import java.util.ArrayList;
import java.util.List;
import level.elements.graph.Node;

/**
 * A Chain is a list of nodes where each node is connected to his predecessor and his successor.
 *
 * @author Andre Matutat
 */
public class Chain implements Comparable<Chain> {
    private List<Node> chain;
    private boolean circle = false;

    public Chain() {
        chain = new ArrayList<>();
    }

    public void add(Node n) {
        chain.add(n);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(chain);
    }

    public void setNodes(List<Node> nodes) {
        chain = nodes;
    }

    public boolean isCircle() {
        return circle;
    }

    public void setCircle(boolean b) {
        circle = b;
    }

    @Override
    public int compareTo(Chain o) {
        if (this.equals(o)) return 0;
        if (circle && !o.circle) return -1;
        return Integer.compare(getNodes().size(), o.getNodes().size());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chain)) return false;
        if (o == this) return true;
        return false;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode nit designed";
        return 42; // any arbitrary constant will do
    }
}
