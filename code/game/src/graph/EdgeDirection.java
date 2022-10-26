package graph;

public enum EdgeDirection {
    UP(0),
    RIGHT(1),
    LEFT(2),
    DOWN(3);

    public int value;

    EdgeDirection(int value) {
        this.value = value;
    }

    public static EdgeDirection getOppsit(EdgeDirection direction) {
        return switch (direction) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
