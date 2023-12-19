package task.game.components;

import core.Component;
import core.level.elements.tile.DoorTile;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates a Collection of {@link DoorTile} to be connected with an entity.
 *
 * <p>Should be used in conjunction with the {@link TaskComponent}.
 *
 * <p>In combination, the {@link TaskComponent#DOOR_OPENER} Consumer can be used to open the
 * corresponding doors in the level when a task is activated.
 *
 * <p>To achieve this, store the resulting door from connecting the level graphs of individual tasks
 * in this component and attach it to the managing entity. Then, use the callback in conjunction
 * with the TaskComponent's DOOR_OPENER Consumer to open the door when the task is activated.
 */
public final class DoorComponent implements Component {

  private final Set<DoorTile> doors;

  /**
   * Creates a new DoorOpenerComponent.
   *
   * @param doors The DoorTiles to store in this component.
   */
  public DoorComponent(final Set<DoorTile> doors) {
    this.doors = doors;
  }

  /**
   * Returns the stored doorTiles in this component.
   *
   * @return The DoorTiles stored in this component.
   */
  public Set<DoorTile> doors() {
    return new HashSet<>(doors);
  }
}
