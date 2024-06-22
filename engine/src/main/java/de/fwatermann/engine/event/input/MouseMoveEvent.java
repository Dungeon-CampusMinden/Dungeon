package de.fwatermann.engine.event.input;

import de.fwatermann.engine.event.Cancelable;
import de.fwatermann.engine.event.Event;
import org.joml.Vector2i;

/**
 * Represents a mouse move event.
 * This class implements the Cancelable interface and provides additional information about a mouse move event.
 */
public class MouseMoveEvent extends Event implements Cancelable {

    /**
     * Indicates whether the event is canceled.
     */
    private boolean canceled = false;

    /**
     * The initial position of the mouse.
     */
    public final Vector2i from;

    /**
     * The final position of the mouse.
     */
    public final Vector2i to;

    /**
     * Constructs a new MouseMoveEvent with the specified initial and final positions.
     *
     * @param from the initial position of the mouse
     * @param to   the final position of the mouse
     */
    public MouseMoveEvent(Vector2i from, Vector2i to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Checks if the event is canceled.
     *
     * @return true if the event is canceled, false otherwise
     */
    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    /**
     * Sets the canceled status of the event.
     *
     * @param canceled the new canceled status
     */
    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
