package levelgraph;

public enum DoorDirection {
    UP(0),
    RIGHT(1),
    LEFT(2),
    DOWN(3);

    public int value;

    DoorDirection(int value) {
        this.value = value;
    }

    public static DoorDirection getOppsit(DoorDirection direction) {
        return switch (direction) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
