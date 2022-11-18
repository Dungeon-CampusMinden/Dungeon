package minimap;

import graph.Graph;
import graph.Node;
import java.util.HashMap;
import java.util.LinkedHashMap;
import levelgraph.GraphLevelGenerator;
import room.Room;

public class TextMap implements IMinimap {

    private Graph<String> graph;
    private HashMap<Node<String>, Boolean> found;

    public TextMap(Graph<String> graph) {
        this.graph = graph;
        found = new LinkedHashMap<>();
    }

    @Override
    public void drawOnMap(char c, Room r) {
        found.put(GraphLevelGenerator.levelNodeToNode.get(r.getLevelNode()), true);
    }

    @Override
    public void drawMap() {
        // todo
    }
}
