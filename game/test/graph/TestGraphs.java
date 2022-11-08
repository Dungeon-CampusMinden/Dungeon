package graph;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestGraphs {
    @Test
    public void testProperties() {
        Property<Integer> prop = new Property<>(42);

        GraphNode<String> nodeA = new GraphNode<>("A");
        nodeA.getAttributes().addAttribute("testProperty", prop);

        GraphNode<String> nodeB = new GraphNode<>("B");
        nodeB.getAttributes().addAttribute("otherProperty", prop);

        GraphEdge edge = new GraphEdge(GraphEdge.Type.undirected, nodeA, nodeB);
        Property<String> nameProperty = new Property<>("Kuckuck");
        edge.getAttributes().addAttribute("name", nameProperty);

        var gotAttribute = nodeA.getAttributes().getAttribute("testProperty");
        assertEquals(prop, gotAttribute);

        gotAttribute = edge.getAttributes().getAttribute("name");
        assertEquals(nameProperty, gotAttribute);

        assertEquals(gotAttribute.value(), "Kuckuck");
    }
}
