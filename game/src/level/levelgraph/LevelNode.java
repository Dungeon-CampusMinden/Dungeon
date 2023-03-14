package level.levelgraph;

import java.util.ArrayList;
import java.util.Collections;
import level.elements.tile.DoorTile;
import level.room.IRoom;

/**
 * A Node in the Graph for a Level. A LeveNode can have a maximum of four edges
 *
 * @param <T> Each Node is a Room in the Level
 */
public class LevelNode<T extends IRoom> {
    private T room;
    private LevelNode[] neighbours;

    private DoorTile.DoorColor[] colors;

    private ArrayList<DoorDirection> toTry = new ArrayList<>();

    public LevelNode() {
        neighbours = new LevelNode[4];
        colors = new DoorTile.DoorColor[4];
        toTry.add(DoorDirection.UP);
        toTry.add(DoorDirection.LEFT);
        toTry.add(DoorDirection.RIGHT);
        toTry.add(DoorDirection.DOWN);
    }

    /**
     * Connect two nodes on the given direction
     *
     * @param other Other Node
     * @param direction Direction to connect the nodes
     * @param onedirectedEdge if true, the connection is one directed
     * @return if connection was successful
     */
    public boolean connect(
            LevelNode other,
            DoorDirection direction,
            boolean onedirectedEdge,
            DoorTile.DoorColor color) {
        if (neighbours[direction.getValue()] == null) {
            if (onedirectedEdge
                    || other.connect(this, DoorDirection.getOpposite(direction), true, color)) {
                neighbours[direction.getValue()] = other;
                colors[direction.getValue()] = color;
                return true;
            }
        }
        return false;
    }
    /**
     * Connect two nodes on the given direction
     *
     * @param other Other Node
     * @param direction Direction to connect the nodes
     * @return if connection was successful
     */
    public boolean connect(LevelNode other, DoorDirection direction, DoorTile.DoorColor color) {
        return connect(other, direction, false, color);
    }

    public boolean connect(LevelNode other) {
        Collections.shuffle(toTry);
        for (DoorDirection direction : toTry) {
            if (tryConnect(other, direction)) return true;
        }
        return false;
    }

    private boolean tryConnect(LevelNode other, DoorDirection direction) {
        return connect(other, direction, DoorTile.DoorColor.NONE);
    }

    /**
     * Set the room for this node
     *
     * @param room
     */
    public void setRoom(T room) {
        this.room = room;
    }

    /**
     * Get the neighbour node on a specific direction
     *
     * @param direction
     * @return
     */
    public LevelNode getNeighbour(DoorDirection direction) {
        return neighbours[direction.getValue()];
    }

    /**
     * @return all neighbour nodes
     */
    public LevelNode[] getNeighbours() {
        return neighbours;
    }

    /**
     * @return The room this node "is"
     */
    public T getRoom() {
        return room;
    }

    public DoorDirection[] getNeighboursAsDirection() {
        DoorDirection[] directions = new DoorDirection[4];
        if (neighbours[DoorDirection.UP.getValue()] != null)
            directions[DoorDirection.UP.getValue()] = DoorDirection.UP;
        if (neighbours[DoorDirection.RIGHT.getValue()] != null)
            directions[DoorDirection.RIGHT.getValue()] = DoorDirection.RIGHT;
        if (neighbours[DoorDirection.LEFT.getValue()] != null)
            directions[DoorDirection.LEFT.getValue()] = DoorDirection.LEFT;
        if (neighbours[DoorDirection.DOWN.getValue()] != null)
            directions[DoorDirection.DOWN.getValue()] = DoorDirection.DOWN;
        return directions;
    }

    public DoorTile.DoorColor[] getColors() {
        return colors;
    }
}
