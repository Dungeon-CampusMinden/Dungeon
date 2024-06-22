package de.fwatermann.dungine.event.window;

import de.fwatermann.dungine.event.Event;
import de.fwatermann.dungine.event.Cancelable;
import org.joml.Vector2i;

/**
 * Represents a window move event.
 * This class extends the Event class and is used to signal that a game window has been moved.
 */
public class WindowMoveEvent extends Event implements Cancelable {

    /**
     * The cancel state of the window move event.
     */
    private boolean canceled = false;

    /**
     * The initial position of the game window before the move.
     */
    public final Vector2i from;

    /**
     * The final position of the game window after the move.
     */
    public final Vector2i to;

    /**
     * Constructs a new WindowMoveEvent.
     *
     * @param from the initial position of the game window before the move
     * @param to   the final position of the game window after the move
     */
    public WindowMoveEvent(Vector2i from, Vector2i to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Checks if the window move event has been canceled.
     *
     * @return false, as the window move event cannot be canceled
     */
    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    /**
     * Sets the cancel state of the window move event.
     * This method does not have any effect, as the window move event cannot be canceled.
     *
     * @param canceled the cancel state to set
     */
    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
