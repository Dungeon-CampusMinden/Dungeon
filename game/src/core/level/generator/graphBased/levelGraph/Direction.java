package core.level.generator.graphBased.levelGraph;

/** The different directions in which nodes can be connected to each other. */
public enum Direction {
    NORTH(0),
    EAST(1),
    SOUTH(2),
    WEST(3);

    private final int value;

    Direction(int value) {
        this.value = value;
    }

    /**
     * Retrieves the opposite direction.
     *
     * @param from The direction from which the opposite direction is sought.
     * @return The opposite direction.
     */
    public static Direction opposite(Direction from) {
        return switch (from) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }

    /**
     * Returns the Direction enum value corresponding to the given integer value.
     *
     * @param v The integer value representing a direction.
     * @return The Direction enum value, or null if the value does not correspond to any direction.
     */
    public static Direction of(int v) {
        return switch (v) {
            case 0 -> NORTH;
            case 1 -> EAST;
            case 2 -> SOUTH;
            case 3 -> WEST;
            default -> null;
        };
    }

    public int value() {
        return value;
    }
}
