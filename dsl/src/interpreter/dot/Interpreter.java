package interpreter.dot;

import java.util.Dictionary;
import java.util.Hashtable;
import parser.AST.*;

public class Interpreter implements AstVisitor<graph.Node<String>> {
    // how to build graph?
    // - need nodes -> hashset, quasi symboltable
    Dictionary<String, graph.Node<String>> graphNodes = new Hashtable<>();

    // - need edges (between two nodes)
    //      -> hashset with string-concat of Names with edge_op as key
    Dictionary<String, graph.Edge> graphEdges = new Hashtable<>();

    @Override
    public graph.Node<String> visit(Node node) {
        // traverse down..
        for (Node child : node.getChildren()) {
            child.accept(this);
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
    public graph.Node<String> visit(BinaryNode node) {
        return null;
    }

    @Override
    public graph.Node<String> visit(DotDefNode node) {
        this.graphEdges = new Hashtable<>();
        this.graphNodes = new Hashtable<>();

        String name = node.getGraphId();

        for (Node edgeStmt : node.getStmtNodes()) {
            edgeStmt.accept(this);
        }

        // TODO: cleanup and package in graph class
        // for testing
        System.out.println("parsed graph [" + name + "]");
        var edgeIter = graphEdges.elements().asIterator();
        while (edgeIter.hasNext()) {
            var edge = edgeIter.next();
            System.out.println("Edge: [" + edge.getName() + "]");
        }

        return null;
    }

    @Override
    public graph.Node<String> visit(EdgeRhsNode node) {
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

            graph.Edge.Type edgeType =
                    edgeRhs.getEdgeOpType().equals(EdgeOpNode.Type.arrow)
                            ? graph.Edge.Type.directed
                            : graph.Edge.Type.undirected;

            var graphEdge = new graph.Edge(edgeType, lhsDotNode, rhsDotNode);
            graphEdges.put(graphEdge.getName(), graphEdge);

            lhsDotNode = rhsDotNode;
        }

        return null;
    }

    @Override
    public graph.Node<String> visit(EdgeOpNode node) {
        return null;
    }
}
