package core.level.levelgraph;

import core.level.elements.tile.DoorTile;
import core.level.room.IRoom;

import java.util.ArrayList;
import java.util.Collections;

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
        if (neighbours[direction.value()] == null) {
            if (onedirectedEdge
                    || other.connect(this, DoorDirection.opposite(direction), true, color)) {
                neighbours[direction.value()] = other;
                colors[direction.value()] = color;
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
    public void room(T room) {
        this.room = room;
    }

    /**
     * Get the neighbour node on a specific direction
     *
     * @param direction
     * @return
     */
    public LevelNode neighbourAt(DoorDirection direction) {
        return neighbours[direction.value()];
    }

    /**
     * @return all neighbour nodes
     */
    public LevelNode[] neighbours() {
        return neighbours;
    }

    /**
     * @return The room this node "is"
     */
    public T room() {
        return room;
    }

    public DoorDirection[] neighboursAsDirection() {
        DoorDirection[] directions = new DoorDirection[4];
        if (neighbours[DoorDirection.UP.value()] != null)
            directions[DoorDirection.UP.value()] = DoorDirection.UP;
        if (neighbours[DoorDirection.RIGHT.value()] != null)
            directions[DoorDirection.RIGHT.value()] = DoorDirection.RIGHT;
        if (neighbours[DoorDirection.LEFT.value()] != null)
            directions[DoorDirection.LEFT.value()] = DoorDirection.LEFT;
        if (neighbours[DoorDirection.DOWN.value()] != null)
            directions[DoorDirection.DOWN.value()] = DoorDirection.DOWN;
        return directions;
    }

    public DoorTile.DoorColor[] colors() {
        return colors;
    }
}
