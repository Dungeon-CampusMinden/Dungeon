package contrib.level.generator.graphBased.levelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class LevelGraphTest {

    private LevelGraph graph;
    @Before
    public void setup(){
        graph=new LevelGraph();
        Node n1 = new Node(null,graph);
        Node n2 = new Node(null,graph);
        Node n3 = new Node(null,graph);
        Node n4 = new Node(null,graph);
        Node n5 = new Node(null,graph);
        n1.forceNeighbor(n2,Direction.NORTH);
        n1.forceNeighbor(n3,Direction.EAST);
        n1.forceNeighbor(n4,Direction.SOUTH);
        n1.forceNeighbor(n5,Direction.WEST);
        graph.addNodes(Set.of(n1,n2,n3,n4,n5));

    }


    @Test
    public void adapter_node_onNode(){

    }

    @Test
    public void adapter_node_onGraph(){

    }
}
