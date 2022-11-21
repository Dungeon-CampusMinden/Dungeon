package collision;

public enum CharacterDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    NONE;

    public CharacterDirection inverse() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case NONE -> NONE;
        };
    }
}
