package level.levelgraph;

import java.util.LinkedHashSet;
import level.elements.tile.DoorTile;
import level.elements.tile.Tile;
import level.room.IRoom;
import level.room.RoomGenerator;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelSize;

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
        createRooms(root, new LinkedHashSet());
        findDoors(root, new LinkedHashSet());
    }

    // Visit all Nodes and create a room for each of them
    private void createRooms(LevelNode node, LinkedHashSet<LevelNode> visited) {
        if (node == null || visited.contains(node)) return;
        node.setRoom(generator.getLevel(designLabel, size, node.getNeighboursAsDirection(), node));
        visited.add(node);
        for (LevelNode neighbour : node.getNeighbours()) createRooms(neighbour, visited);
    }

    // Add the connection between the doors
    private void findDoors(LevelNode node, LinkedHashSet<LevelNode> visited) {
        if (node == null || visited.contains(node)) return;
        visited.add(node);

        LevelNode rightNeighbour = node.getNeighbour(DoorDirection.RIGHT);
        LevelNode leftNeighbour = node.getNeighbour(DoorDirection.LEFT);
        LevelNode lowerNeighbour = node.getNeighbour(DoorDirection.DOWN);
        LevelNode upperNeighbour = node.getNeighbour(DoorDirection.UP);
        DoorTile[] doors = findDoorsInRoom(node.getRoom());

        if (rightNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.RIGHT.getValue()],
                    findDoorsInRoom(rightNeighbour.getRoom())[DoorDirection.LEFT.getValue()],
                    DoorDirection.RIGHT,
                    node);
        if (leftNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.LEFT.getValue()],
                    findDoorsInRoom(leftNeighbour.getRoom())[DoorDirection.RIGHT.getValue()],
                    DoorDirection.LEFT,
                    node);
        if (lowerNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.DOWN.getValue()],
                    findDoorsInRoom(lowerNeighbour.getRoom())[DoorDirection.UP.getValue()],
                    DoorDirection.DOWN,
                    node);
        if (upperNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.UP.getValue()],
                    findDoorsInRoom(upperNeighbour.getRoom())[DoorDirection.DOWN.getValue()],
                    DoorDirection.UP,
                    node);

        for (LevelNode child : node.getNeighbours()) findDoors(child, visited);
    }

    private void doorPairFound(
            DoorTile door, DoorTile otherDoor, DoorDirection direction, LevelNode node) {
        door.setOtherDoor(otherDoor);
        door.setColor(node.getColors()[direction.getValue()]);
        findDoorstep(door, direction, node.getRoom());
    }

    private DoorTile[] findDoorsInRoom(IRoom room) {
        DoorTile[] doorsInOrder = new DoorTile[4];
        for (DoorTile door : room.getDoors()) {
            if (isAccessible(door.getCoordinate(), room.getLayout(), DoorDirection.DOWN)) {
                doorsInOrder[DoorDirection.UP.getValue()] = door;
            } else if (isAccessible(door.getCoordinate(), room.getLayout(), DoorDirection.LEFT)) {
                doorsInOrder[DoorDirection.RIGHT.getValue()] = door;
            } else if (isAccessible(door.getCoordinate(), room.getLayout(), DoorDirection.RIGHT)) {
                doorsInOrder[DoorDirection.LEFT.getValue()] = door;
            } else if (isAccessible(door.getCoordinate(), room.getLayout(), DoorDirection.UP)) {
                doorsInOrder[DoorDirection.DOWN.getValue()] = door;
            }
        }
        return doorsInOrder;
    }

    private boolean isAccessible(Coordinate c, Tile[][] layout, DoorDirection direction) {
        try {

            switch (direction) {
                case UP:
                    return layout[c.y + 1][c.x].isAccessible();
                case DOWN:
                    return layout[c.y - 1][c.x].isAccessible();
                case LEFT:
                    return layout[c.y][c.x - 1].isAccessible();
                case RIGHT:
                    return layout[c.y][c.x + 1].isAccessible();
                default:
                    return false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private void findDoorstep(DoorTile door, DoorDirection direction, IRoom room) {
        Tile doorstep = null;
        Coordinate doorCoordinate = door.getCoordinate();
        Tile[][] layout = room.getLayout();
        switch (direction) {
            case UP:
                doorstep = layout[doorCoordinate.y - 1][doorCoordinate.x];
                break;
            case RIGHT:
                doorstep = layout[doorCoordinate.y][doorCoordinate.x - 1];
                break;
            case LEFT:
                doorstep = layout[doorCoordinate.y][doorCoordinate.x + 1];
                break;
            case DOWN:
                doorstep = layout[doorCoordinate.y + 1][doorCoordinate.x];
                break;
        }
        if (doorstep == null) throw new NullPointerException("DoorStep not found");
        door.setDoorstep(doorstep);
    }

    /**
     * @return The Room that is saved in the root-Node
     */
    public IRoom getRootRoom() {
        return root.getRoom();
    }
}
