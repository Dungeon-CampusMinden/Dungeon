package de.fwatermann.engine.event;

/**
 * Represents a cancelable event.
 * This interface provides methods to check if an event is canceled and to set the canceled status of an event.
 */
public interface Cancelable {

    /**
     * Checks if the event is canceled.
     *
     * @return true if the event is canceled, false otherwise
     */
    boolean isCanceled();

    /**
     * Sets the canceled status of the event.
     *
     * @param canceled the new canceled status
     */
    void setCanceled(boolean canceled);

}
