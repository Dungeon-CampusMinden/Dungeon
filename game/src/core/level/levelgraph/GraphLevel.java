package core.level.levelgraph;

import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.room.IRoom;
import core.level.room.RoomGenerator;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;

import java.util.LinkedHashSet;

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
        node.room(generator.level(designLabel, size, node.neighboursAsDirection(), node));
        visited.add(node);
        for (LevelNode neighbour : node.neighbours()) createRooms(neighbour, visited);
    }

    // Add the connection between the doors
    private void findDoors(LevelNode node, LinkedHashSet<LevelNode> visited) {
        if (node == null || visited.contains(node)) return;
        visited.add(node);

        LevelNode rightNeighbour = node.neighbourAt(DoorDirection.RIGHT);
        LevelNode leftNeighbour = node.neighbourAt(DoorDirection.LEFT);
        LevelNode lowerNeighbour = node.neighbourAt(DoorDirection.DOWN);
        LevelNode upperNeighbour = node.neighbourAt(DoorDirection.UP);
        DoorTile[] doors = findDoorsInRoom(node.room());

        if (rightNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.RIGHT.value()],
                    findDoorsInRoom(rightNeighbour.room())[DoorDirection.LEFT.value()],
                    DoorDirection.RIGHT,
                    node);
        if (leftNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.LEFT.value()],
                    findDoorsInRoom(leftNeighbour.room())[DoorDirection.RIGHT.value()],
                    DoorDirection.LEFT,
                    node);
        if (lowerNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.DOWN.value()],
                    findDoorsInRoom(lowerNeighbour.room())[DoorDirection.UP.value()],
                    DoorDirection.DOWN,
                    node);
        if (upperNeighbour != null)
            doorPairFound(
                    doors[DoorDirection.UP.value()],
                    findDoorsInRoom(upperNeighbour.room())[DoorDirection.DOWN.value()],
                    DoorDirection.UP,
                    node);

        for (LevelNode child : node.neighbours()) findDoors(child, visited);
    }

    private void doorPairFound(
            DoorTile door, DoorTile otherDoor, DoorDirection direction, LevelNode node) {
        door.setOtherDoor(otherDoor);
        door.setColor(node.colors()[direction.value()]);
        findDoorstep(door, direction, node.room());
    }

    private DoorTile[] findDoorsInRoom(IRoom room) {
        DoorTile[] doorsInOrder = new DoorTile[4];
        for (DoorTile door : room.doors()) {
            if (isAccessible(door.coordinate(), room.layout(), DoorDirection.DOWN)) {
                doorsInOrder[DoorDirection.UP.value()] = door;
            } else if (isAccessible(door.coordinate(), room.layout(), DoorDirection.LEFT)) {
                doorsInOrder[DoorDirection.RIGHT.value()] = door;
            } else if (isAccessible(door.coordinate(), room.layout(), DoorDirection.RIGHT)) {
                doorsInOrder[DoorDirection.LEFT.value()] = door;
            } else if (isAccessible(door.coordinate(), room.layout(), DoorDirection.UP)) {
                doorsInOrder[DoorDirection.DOWN.value()] = door;
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
        Coordinate doorCoordinate = door.coordinate();
        Tile[][] layout = room.layout();
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
    public IRoom rootRoom() {
        return root.room();
    }
}
