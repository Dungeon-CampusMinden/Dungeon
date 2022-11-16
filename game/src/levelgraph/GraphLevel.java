package levelgraph;

import java.util.LinkedHashSet;
import level.tools.DesignLabel;
import level.tools.LevelSize;
import roomlevel.IRoom;

public class GraphLevel {
    private LevelNode root;
    private LevelSize size;
    private DesignLabel designLabel;
    private RoomGenerator generator;

    public GraphLevel(LevelNode root, LevelSize size, DesignLabel designLabel) {
        this.root = root;
        this.designLabel = designLabel;
        this.size = size;
        generator = new RoomGenerator(); // todo replace
        LinkedHashSet<LevelNode> visited = new LinkedHashSet<>();
        createRooms(root, visited);
        connectDoors();
    }

    private void createRooms(LevelNode node, LinkedHashSet<LevelNode> visited) {
        if (visited.contains(node)) return;
        node.setRoom(generator.generateRoom(size, designLabel));
        visited.add(node);
        for (LevelNode neighbour : node.getNeighbours()) createRooms(neighbour, visited);
    }

    private void connectDoors() {
        // todo
    }

    public IRoom getRootRoom() {
        return root.getRoom();
    }
}
