package levelgraph;

/**
 * Defines on which side of the room the door is located
 *
 * @quthor Andre Matutat
 */
public enum DoorDirection {
    UP(0),
    RIGHT(1),
    LEFT(2),
    DOWN(3);

    private int value;

    DoorDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Returns the opposite side of the room
     *
     * @param direction
     * @return opposite side of the room
     */
    public static DoorDirection getOpposite(DoorDirection direction) {
        return switch (direction) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
