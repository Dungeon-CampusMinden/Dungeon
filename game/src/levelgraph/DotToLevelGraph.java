package levelgraph;

import graph.Graph;
import level.elements.tile.DoorTile;

public class DotToLevelGraph {

    public static LevelNode convert(Graph<String> levelGenGraph) {
        // todo convert
        LevelNode root = new LevelNode();
        root.connect(new LevelNode(), DoorDirection.LEFT, DoorTile.DoorColor.BLUE);
        root.connect(new LevelNode(), DoorDirection.RIGHT, DoorTile.DoorColor.RED);
        root.connect(new LevelNode(), DoorDirection.DOWN, DoorTile.DoorColor.GREEN);
        return root;
    }
}
