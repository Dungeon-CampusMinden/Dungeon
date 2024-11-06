package newdsl.graph;

import java.util.ArrayList;
import java.util.Iterator;

public class TaskDependencyGraph {
    private final ArrayList<TaskEdge> edges;
    private final ArrayList<TaskNode> nodes;

    public TaskDependencyGraph(ArrayList<TaskEdge> edges, ArrayList<TaskNode> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }

    public Iterator<TaskEdge> edgeIterator() {
        return edges.iterator();
    }

    public Iterator<TaskNode> nodeIterator() {
        return nodes.iterator();
    }

    public ArrayList<TaskEdge> getEdges() {
        return edges;
    }

    public ArrayList<TaskNode> getNodes() {
        return nodes;
    }
}
