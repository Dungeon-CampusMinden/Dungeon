package levelgraph;

public class LevelNode<T extends Object> {
    private T value;
    private LevelNode[] neighbours;

    public LevelNode(T value) {
        this.value = value;
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

    public void setValue(T value) {
        this.value = value;
    }

    public LevelNode getNeighbour(DoorDirection direction) {
        return neighbours[direction.value];
    }

    public LevelNode[] getNeighbours() {
        return neighbours;
    }

    public T getValue() {
        return value;
    }
}
