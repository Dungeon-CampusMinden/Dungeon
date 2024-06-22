package de.fwatermann.engine.event.input;

import de.fwatermann.engine.event.Event;

/**
 * Represents a keyboard event.
 * This class extends the Event class and provides additional information about a keyboard event.
 */
public class KeyboardEvent extends Event {

    /**
     * The key code of the key involved in the event.
     */
    public final int key;

    /**
     * The action performed on the key.
     */
    public final KeyAction action;

    /**
     * Constructs a new KeyboardEvent with the specified key code and action.
     *
     * @param key    the key code of the key involved in the event
     * @param action the action performed on the key
     */
    public KeyboardEvent(int key, KeyAction action) {
        this.key = key;
        this.action = action;
    }

    /**
     * Enum representing the possible actions that can be performed on a key.
     */
    public enum KeyAction {
        /**
         * Represents the pressing of a key.
         */
        PRESS,

        /**
         * Represents the releasing of a key.
         */
        RELEASE,

        /**
         * Represents the repeated pressing of a key.
         */
        REPEAT
    }

}
