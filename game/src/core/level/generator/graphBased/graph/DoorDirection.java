package core.level.generator.graphBased.graph;

/**
 * Defines on which side of the room the door is located
 *
 * @quthor Andre Matutat
 */
public enum DoorDirection {
    UP(0),

    LEFT(1),
    DOWN(2),
    RIGHT(3);

    private int value;

    DoorDirection(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    /**
     * Returns the opposite side of the room
     *
     * @param direction
     * @return opposite side of the room
     */
    public static DoorDirection opposite(DoorDirection direction) {
        return switch (direction) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
