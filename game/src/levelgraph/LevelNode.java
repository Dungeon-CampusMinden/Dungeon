package levelgraph;

import roomlevel.IRoom;

public class LevelNode<T extends IRoom> {
    private T room;
    private LevelNode[] neighbours;

    public LevelNode() {
        neighbours = new LevelNode[4];
    }

    public boolean connect(LevelNode other, DoorDirection direction, boolean skipLoop) {
        if (neighbours[direction.value] == null) {
            if (skipLoop || other.connect(this, DoorDirection.getOppsit(direction), true)) {
                neighbours[direction.value] = other;
                return true;
            }
        }
        return false;
    }

    public boolean connect(LevelNode other, DoorDirection direction) {
        return connect(other, direction, false);
    }

    public void setRoom(T room) {
        this.room = room;
    }

    public LevelNode getNeighbour(DoorDirection direction) {
        return neighbours[direction.value];
    }

    public LevelNode[] getNeighbours() {
        return neighbours;
    }

    public T getRoom() {
        return room;
    }
}
