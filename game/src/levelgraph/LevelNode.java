package levelgraph;

import level.elements.tile.DoorTile;
import room.IRoom;

/**
 * A Node in the Graph for a Level. A LeveNode can have a maximum of four edges
 *
 * @param <T> Each Node is a Room in the Level
 */
public class LevelNode<T extends IRoom> {
    private T room;
    private LevelNode[] neighbours;

    private DoorTile.DoorColor [] colors;

    public LevelNode() {
        neighbours = new LevelNode[4];
        colors = new DoorTile.DoorColor[4];
    }

    /**
     * Connect two nodes on the given direction
     *
     * @param other Other Node
     * @param direction Direction to connect the nodes
     * @param onedirectedEdge if true, the connection is one directed
     * @return if connection was successful
     */
    public boolean connect(LevelNode other, DoorDirection direction, boolean onedirectedEdge, DoorTile.DoorColor color) {
        if (neighbours[direction.getValue()] == null) {
            if (onedirectedEdge || other.connect(this, DoorDirection.getOppsit(direction), true,color)) {
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
    public boolean connect(LevelNode other, DoorDirection direction,DoorTile.DoorColor color) {
        return connect(other, direction, false,color);
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
        if (neighbours[0] != null) directions[0] = DoorDirection.UP;
        if (neighbours[1] != null) directions[1] = DoorDirection.RIGHT;
        if (neighbours[2] != null) directions[2] = DoorDirection.LEFT;
        if (neighbours[3] != null) directions[3] = DoorDirection.DOWN;
        return directions;
    }

    public DoorTile.DoorColor[] getColors() {
        return colors;
    }
}
