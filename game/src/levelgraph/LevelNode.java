package levelgraph;

import room.IRoom;

/**
 * A Node in the Graph for a Level. A LeveNode can have a maximum of four edges
 *
 * @param <T> Each Node is a Room in the Level
 */
public class LevelNode<T extends IRoom> {
    private T room;
    private LevelNode[] neighbours;

    public LevelNode() {
        neighbours = new LevelNode[4];
    }

    /**
     * Connect two nodes on the given direction
     *
     * @param other Other Node
     * @param direction Direction to connect the nodes
     * @param onedirectedEdge if true, the connection is one directed
     * @return if connection was successful
     */
    public boolean connect(LevelNode other, DoorDirection direction, boolean onedirectedEdge) {
        if (neighbours[direction.getValue()] == null) {
            if (onedirectedEdge || other.connect(this, DoorDirection.getOppsit(direction), true)) {
                neighbours[direction.getValue()] = other;
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
    public boolean connect(LevelNode other, DoorDirection direction) {
        return connect(other, direction, false);
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
}
