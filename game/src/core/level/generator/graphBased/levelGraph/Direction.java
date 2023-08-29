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

    public int value() {
        return value;
    }
}
