package de.fwatermann.dungine.event.window;

import de.fwatermann.dungine.event.Event;
import de.fwatermann.dungine.window.GameWindow;
import de.fwatermann.dungine.event.Cancelable;

/**
 * Represents a window close event.
 * This class extends the Event class and is used to signal that a game window has been closed.
 */
public class WindowCloseEvent extends Event implements Cancelable {

    private boolean canceled = false;

    /**
     * The game window that has been closed.
     */
    public final GameWindow window;

    /**
     * Constructs a new WindowCloseEvent.
     *
     * @param window the game window that has been closed
     */
    public WindowCloseEvent(GameWindow window) {
        this.window = window;
    }

    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
