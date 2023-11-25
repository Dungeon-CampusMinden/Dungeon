package core.level.utils;

import core.utils.Constants;

/** Each type of field in a level can be represented by an integer value. */
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

    /**
     * Represents a level element with accessibility information.
     *
     * @param value The accessibility value of the element.
     */
    LevelElement(boolean value) {
        this.value = value;
    }

    /**
     * Checks if the element is accessible.
     *
     * @return true if the element is accessible, code false if not.
     */
    public boolean value() {
        return value;
    }
}
