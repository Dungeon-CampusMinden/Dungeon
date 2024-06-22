package de.fwatermann.engine.event;

/**
 * Represents an abstract event.
 * This class serves as a base for all other event classes in the application.
 */
public abstract class Event {

    /**
     * Fires the event.
     * This method triggers the event by calling the {@link EventManager#fireEvent fireEvent} method of the EventManager singleton instance.
     */
    public void fire() {
        EventManager.getInstance().fireEvent(this);
    }

}