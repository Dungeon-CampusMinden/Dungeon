package level.tools;

import tools.Constants;

/**
 * Each type of field in a level can be represented by an integer value.
 *
 * @author Andre Matutat
 */
public enum LevelElement {
    /** This field is a blank */
    SKIP(Constants.LEVELELEMENT_IS_NOT_ACCESSIBLE),
    /** This field is a floor-field */
    FLOOR(Constants.LEVELELEMENT_IS_ACCESSIBLE),
    /** This field is a wall-field */
    WALL(Constants.LEVELELEMENT_IS_NOT_ACCESSIBLE),
    /** This field is a hole-field */
    HOLE(Constants.LEVELELEMENT_IS_NOT_ACCESSIBLE),
    /** This field is the exit-field to the next level */
    EXIT(Constants.LEVELELEMENT_IS_ACCESSIBLE),

    DOOR(Constants.LEVELELEMENT_IS_ACCESSIBLE);

    private final boolean value;

    LevelElement(boolean value) {
        this.value = value;
    }

    /**
     * @return A random enum-value
     */
    public boolean getValue() {
        return value;
    }
}
