package graph;

import static org.junit.Assert.assertEquals;

import dslToGame.graph.Edge;
import dslToGame.graph.Node;
import dslToGame.graph.Property;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class TestGraphs {

    /**
     * Tests the property bag of graph nodes and checks, whether the stored values and returned
     * values are equal
     */
    @Test
    public void testProperties() {
        Property<Integer> prop = new Property<>(42);

        Node<String> nodeA = new Node<>("A");
        nodeA.getAttributes().addProperty("testProperty", prop);

        Node<String> nodeB = new Node<>("B");
        nodeB.getAttributes().addProperty("otherProperty", prop);

        Edge edge = new Edge(Edge.Type.undirected, nodeA, nodeB);
        Property<String> nameProperty = new Property<>("Kuckuck");
        edge.getAttributes().addProperty("name", nameProperty);

        var gotAttribute = nodeA.getAttributes().getAttribute("testProperty");
        assertEquals(prop, gotAttribute);

        gotAttribute = edge.getAttributes().getAttribute("name");
        assertEquals(nameProperty, gotAttribute);

        assertEquals(gotAttribute.value(), "Kuckuck");
    }

    /** Tests, if the edge iterator iterates in the correct order */
    @Test
    public void testEdgeIterator() {
        Node<String> stringNode = new Node<>("Hello");
        Node<Integer> intNode = new Node<>(42);
        Node<Float> floatNode = new Node<>(3.14f);

        Edge edge1 = new Edge(stringNode, intNode);
        Edge edge2 = new Edge(intNode, floatNode);
        Edge edge3 = new Edge(floatNode, stringNode);

        var edgeIter = stringNode.edgeIterator();
        AtomicInteger edgeCount = new AtomicInteger(0);
        edgeIter.forEachRemaining(elem -> edgeCount.addAndGet(1));
        assertEquals(2, edgeCount.get());

        edgeIter = stringNode.edgeIterator();
        var returnedEdge1 = edgeIter.next();
        assertEquals(returnedEdge1, edge1);
        var returnedEdge2 = edgeIter.next();
        assertEquals(returnedEdge2, edge3);
    }
}
