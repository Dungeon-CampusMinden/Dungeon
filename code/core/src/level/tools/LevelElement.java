package level.tools;

/**
 * Each type of field in a level can be represented by an integer value.
 *
 * @author Andre Matutat
 */
public enum LevelElement {
    SKIP(-2),
    /** This field can be replaced with another one */
    WILD(-1),
    /** This field is a floor-field */
    FLOOR(0),
    /** This field is a wall-field */
    WALL(1),
    /** This field is the exit-field to the next level */
    EXIT(3),
    DOOR(4);

    private int value;

    LevelElement(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
