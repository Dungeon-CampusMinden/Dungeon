package levelgraph;

import java.util.LinkedHashSet;
import level.elements.tile.DoorTile;
import level.elements.tile.Tile;
import level.tools.DesignLabel;
import level.tools.LevelSize;
import room.IRoom;
import room.RoomGenerator;

/**
 * @author Andre Matutat
 */
public class GraphLevel {
    private LevelNode root;
    private LevelSize size;
    private DesignLabel designLabel;
    private RoomGenerator generator;

    /**
     * @param root Root-Node of the graph
     * @param size The level size
     * @param designLabel The design of the rooms
     */
    public GraphLevel(LevelNode root, LevelSize size, DesignLabel designLabel) {
        this.root = root;
        this.designLabel = designLabel;
        this.size = size;
        generator = new RoomGenerator();
        LinkedHashSet<LevelNode> visited = new LinkedHashSet<>();
        createRooms(root, visited);
        findDoors();
    }

    // Visit all Nodes and create a room for each of them
    private void createRooms(LevelNode node, LinkedHashSet<LevelNode> visited) {
        if (node == null || visited.contains(node)) return;
        node.setRoom(generator.getLevel(designLabel, size, node.getNeighboursAsDirection()));
        visited.add(node);
        for (LevelNode neighbour : node.getNeighbours()) createRooms(neighbour, visited);
    }

    // Add the connection between the doors
    private void findDoors() {
        // todo loop over all doors, dont look up
        //     private static String findTexturePathDoor(LevelPart levelPart) <- TileTextureFactory
        // doorstep setzen hier?!
        // refactor seperate method
        DoorTile door = null;
        DoorTile otherDoor = null;

        connectDoors(door, otherDoor);
        findDoorstep(door);
        findDoorstep(otherDoor);
    }

    private void connectDoors(DoorTile a, DoorTile b) {
        a.setOtherDoor(b);
        a.setColor(getColor());
        b.setOtherDoor(a);
        b.setColor(getColor());
    }

    private DoorTile.DoorColor getColor() {
        // todo get color from graph
        //     private static String findTexturePathDoor(LevelPart levelPart) <- TileTextureFactory
        return DoorTile.DoorColor.BLUE;
    }

    private void findDoorstep(DoorTile door) {
        Tile doorstep = null; // todo
        door.setDoorstep(doorstep);
    }

    /**
     * @return The Room that is saved in the root-Node
     */
    public IRoom getRootRoom() {
        return root.getRoom();
    }
}
