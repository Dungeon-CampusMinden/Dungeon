package interpreter.dot;

import graph.Edge;
import graph.Graph;
// CHECKSTYLE:OFF: AvoidStarImport

import parser.ast.*;
// CHECKSTYLE:ON: AvoidStarImport

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

public class Interpreter implements AstVisitor<graph.Node<String>> {
    // how to build graph?
    // - need nodes -> hashset, quasi symboltable
    Dictionary<String, graph.Node<String>> graphNodes = new Hashtable<>();

    // - need edges (between two nodes)
    //      -> hashset with string-concat of Names with edge_op as key
    Dictionary<String, Edge> graphEdges = new Hashtable<>();

    ArrayList<Graph<String>> graphs = new ArrayList<>();

    /**
     * Parses a dot definition and creates a {@link Graph} from it
     *
     * @param dotDefinition The DotDefNode to parse as a graph
     * @return The {@link Graph} object created from the dotDefinition
     */
    public Graph<String> getGraph(DotDefNode dotDefinition) {
        graphNodes = new Hashtable<>();
        graphEdges = new Hashtable<>();

        dotDefinition.accept(this);

        // sort edges
        var edgeIter = graphEdges.elements().asIterator();
        ArrayList<Edge> edgeList = new ArrayList<>(graphEdges.size());

        while (edgeIter.hasNext()) {
            edgeList.add(edgeIter.next());
        }

        Collections.sort(edgeList);

        // sort nodes
        var nodeIter = graphNodes.elements().asIterator();
        ArrayList<graph.Node<String>> nodeList = new ArrayList<>(graphNodes.size());

        while (nodeIter.hasNext()) {
            nodeList.add(nodeIter.next());
        }

        Collections.sort(nodeList);

        return new Graph<>(edgeList, nodeList);
    }

    @Override
    public graph.Node<String> visit(Node node) {
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
    public graph.Node<String> visit(IdNode node) {
        String name = node.getName();
        // lookup and create, if not present previously
        if (graphNodes.get(name) == null) {
            graphNodes.put(name, new graph.Node<>(name));
        }

        // return Dot-Node
        return graphNodes.get(name);
    }

    @Override
    public graph.Node<String> visit(DotDefNode node) {
        this.graphEdges = new Hashtable<>();
        this.graphNodes = new Hashtable<>();

        for (Node edgeStmt : node.getStmtNodes()) {
            edgeStmt.accept(this);
        }

        return null;
    }

    @Override
    public graph.Node<String> visit(EdgeStmtNode node) {
        // TODO: add handling of edge-attributes

        // node will contain all edge definitions
        var lhsDotNode = node.getLhsId().accept(this);
        graph.Node<String> rhsDotNode = null;

        for (Node edge : node.getRhsStmts()) {
            assert (edge.type.equals(Node.Type.DotEdgeRHS));

            EdgeRhsNode edgeRhs = (EdgeRhsNode) edge;
            rhsDotNode = (graph.Node<String>) edgeRhs.getIdNode().accept(this);

            Edge.Type edgeType =
                    edgeRhs.getEdgeOpType().equals(EdgeOpNode.Type.arrow)
                            ? Edge.Type.directed
                            : Edge.Type.undirected;

            var graphEdge = new Edge(edgeType, lhsDotNode, rhsDotNode);
            graphEdges.put(graphEdge.name(), graphEdge);

            lhsDotNode = rhsDotNode;
        }

        return null;
    }
}
