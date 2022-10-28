package interpreter.dot;

import java.util.Dictionary;
import java.util.Hashtable;
import parser.AST.*;

public class Interpreter implements AstVisitor<Object> {
    // how to build graph?
    // - need nodes -> hashset, quasi symboltable
    Dictionary<String, GraphNode> graphNodes = new Hashtable<>();

    // - need edges (between two nodes)
    //      -> hashset with string-concat of Names with edge_op as key
    Dictionary<String, GraphEdge> graphEdges = new Hashtable<>();

    @Override
    public Object visit(Node node) {
        // traverse down..
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(IdNode node) {
        String name = node.getName();
        // lookup and create, if not present previously
        if (graphNodes.get(name) == null) {
            graphNodes.put(name, new GraphNode(name));
        }

        // return Dot-Node
        return graphNodes.get(name);
    }

    @Override
    public Object visit(BinaryNode node) {
        return null;
    }

    @Override
    public Object visit(DotDefNode node) {
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
    public Object visit(EdgeRhsNode node) {
        return null;
    }

    @Override
    public Object visit(EdgeStmtNode node) {
        // TODO: add handling of edge-attributes

        // node will contain all edge definitions
        GraphNode lhsDotNode = (GraphNode) node.getLhsId().accept(this);
        GraphNode rhsDotNode = null;

        for (Node edge : node.getRhsStmts()) {
            assert (edge.type.equals(Node.Type.DotEdgeRHS));

            EdgeRhsNode edgeRhs = (EdgeRhsNode) edge;
            rhsDotNode = (GraphNode) edgeRhs.getIdNode().accept(this);

            GraphEdge.Type edgeType =
                    edgeRhs.getEdgeOpType().equals(EdgeOpNode.Type.arrow)
                            ? GraphEdge.Type.directed
                            : GraphEdge.Type.undirected;

            GraphEdge graphEdge = new GraphEdge(edgeType, lhsDotNode, rhsDotNode);
            graphEdges.put(graphEdge.getName(), graphEdge);

            lhsDotNode = rhsDotNode;
        }

        return null;
    }

    @Override
    public Object visit(EdgeOpNode node) {
        return null;
    }
}
