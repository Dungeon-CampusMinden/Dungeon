package interpreter.dot;

import parser.ast.*;
// CHECKSTYLE:ON: AvoidStarImport

import taskDependencyGraph.TaskDependencyGraph;
// CHECKSTYLE:OFF: AvoidStarImport
import taskDependencyGraph.TaskEdge;
import taskDependencyGraph.TaskNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

public class Interpreter implements AstVisitor<TaskNode> {
    // how to build graph?
    // - need nodes -> hashset, quasi symboltable
    Dictionary<String, TaskNode> graphNodes = new Hashtable<>();

    // - need edges (between two nodes)
    //      -> hashset with string-concat of Names with edge_op as key
    Dictionary<String, TaskEdge> graphEdges = new Hashtable<>();

    ArrayList<TaskDependencyGraph> graphs = new ArrayList<>();

    /**
     * Parses a dot definition and creates a {@link TaskDependencyGraph} from it
     *
     * @param dotDefinition The DotDefNode to parse as a graph
     * @return The {@link TaskDependencyGraph} object created from the dotDefinition
     */
    public TaskDependencyGraph getGraph(DotDefNode dotDefinition) {
        graphNodes = new Hashtable<>();
        graphEdges = new Hashtable<>();

        dotDefinition.accept(this);

        // sort edges
        var edgeIter = graphEdges.elements().asIterator();
        ArrayList<TaskEdge> edgeList = new ArrayList<>(graphEdges.size());

        while (edgeIter.hasNext()) {
            edgeList.add(edgeIter.next());
        }

        Collections.sort(edgeList);

        // sort nodes
        var nodeIter = graphNodes.elements().asIterator();
        ArrayList<TaskNode> nodeList = new ArrayList<>(graphNodes.size());

        while (nodeIter.hasNext()) {
            nodeList.add(nodeIter.next());
        }

        Collections.sort(nodeList);

        return new TaskDependencyGraph(edgeList, nodeList);
    }

    @Override
    public TaskNode visit(Node node) {
        // traverse down..
        for (Node child : node.getChildren()) {
            if (child.type == Node.Type.DotDefinition) {
                var graph = getGraph((DotDefNode) child);
                graphs.add(graph);
            } else {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public TaskNode visit(IdNode node) {
        String name = node.getName();
        // lookup and create, if not present previously
        if (graphNodes.get(name) == null) {
            graphNodes.put(name, new TaskNode(name));
        }

        // return Dot-Node
        return graphNodes.get(name);
    }

    @Override
    public TaskNode visit(DotDefNode node) {
        this.graphEdges = new Hashtable<>();
        this.graphNodes = new Hashtable<>();

        for (Node edgeStmt : node.getStmtNodes()) {
            edgeStmt.accept(this);
        }

        return null;
    }

    @Override
    public TaskNode visit(EdgeStmtNode node) {
        // TODO: add handling of edge-attributes

        // node will contain all edge definitions
        var lhsDotNode = node.getLhsId().accept(this);
        TaskNode rhsDotNode = null;

        for (Node edge : node.getRhsStmts()) {
            assert (edge.type.equals(Node.Type.DotEdgeRHS));

            EdgeRhsNode edgeRhs = (EdgeRhsNode) edge;
            rhsDotNode = (TaskNode) edgeRhs.getIdNode().accept(this);

            TaskEdge.Type edgeType =
                    edgeRhs.getEdgeOpType().equals(EdgeOpNode.Type.arrow)
                            ? TaskEdge.Type.directed
                            : TaskEdge.Type.undirected;

            var graphEdge = new TaskEdge(edgeType, lhsDotNode, rhsDotNode);
            graphEdges.put(graphEdge.name(), graphEdge);

            lhsDotNode = rhsDotNode;
        }

        return null;
    }
}
