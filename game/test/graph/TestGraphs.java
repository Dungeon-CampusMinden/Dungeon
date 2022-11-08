package graph;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class TestGraphs {
    @Test
    public void testProperties() {
        Property<Integer> prop = new Property<>(42);

        GraphNode<String> nodeA = new GraphNode<>("A");
        nodeA.getAttributes().addProperty("testProperty", prop);

        GraphNode<String> nodeB = new GraphNode<>("B");
        nodeB.getAttributes().addProperty("otherProperty", prop);

        GraphEdge edge = new GraphEdge(GraphEdge.Type.undirected, nodeA, nodeB);
        Property<String> nameProperty = new Property<>("Kuckuck");
        edge.getAttributes().addProperty("name", nameProperty);

        var gotAttribute = nodeA.getAttributes().getAttribute("testProperty");
        assertEquals(prop, gotAttribute);

        gotAttribute = edge.getAttributes().getAttribute("name");
        assertEquals(nameProperty, gotAttribute);

        assertEquals(gotAttribute.value(), "Kuckuck");
    }

    @Test
    public void testEdgeIterator() {
        GraphNode<String> stringNode = new GraphNode<>("Hello");
        GraphNode<Integer> intNode = new GraphNode<>(42);
        GraphNode<Float> floatNode = new GraphNode<>(3.14f);

        GraphEdge edge1 = new GraphEdge(stringNode, intNode);
        GraphEdge edge2 = new GraphEdge(intNode, floatNode);
        GraphEdge edge3 = new GraphEdge(floatNode, stringNode);

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
