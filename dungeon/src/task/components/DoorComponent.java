package task.components;

import core.Component;
import core.level.elements.tile.DoorTile;

/**
 * Associates a {@link DoorTile} to be connected with an entity.
 *
 * <p>Should be used in conjunction with the {@link TaskComponent}.
 *
 * <p>In combination, the {@link TaskComponent#DOOR_OPENER} Consumer can be used to open the
 * corresponding door in the level when a task is activated.
 *
 * <p>To achieve this, store the resulting door from connecting the level graphs of individual tasks
 * in this component and attach it to the managing entity. Then, use the callback in conjunction
 * with the TaskComponent's DOOR_OPENER Consumer to open the door when the task is activated.
 */
public final class DoorComponent implements Component {

    private final DoorTile door;

    /**
     * Creates a new DoorOpenerComponent.
     *
     * @param door The DoorTile to store in this component.
     */
    public DoorComponent(final DoorTile door) {
        this.door = door;
    }

    /**
     * Returns the stored doorTile in this component.
     *
     * @return The DoorTile stored in this component.
     */
    public DoorTile door() {
        return door;
    }
}
