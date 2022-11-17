package levelgraph;

import graph.Graph;

public class DotToLevelGraph {

    public static LevelNode convert(Graph<String> levelGenGraph) {
        // todo convert
        LevelNode root = new LevelNode();
        root.connect(new LevelNode(), DoorDirection.LEFT);
        root.connect(new LevelNode(), DoorDirection.RIGHT);
        root.connect(new LevelNode(), DoorDirection.DOWN);
        return root;
    }
}
